package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.SmartCardManager
import org.example.project.data.model.DirectIssueRequest
import org.example.project.data.repository.StaffRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class IssueCardState(
    val isWriting: Boolean = false,
    val writeSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val issuedCardID: String? = null
)

class IssueCardViewModel(
    private val repo: StaffRepository = StaffRepository(),
    private val nfc: SmartCardManager = SmartCardManager()
) : ViewModel() {

    private val _state = MutableStateFlow(IssueCardState())
    val state: StateFlow<IssueCardState> = _state

    fun issueCard(name: String, dob: String, phone: String) {
        if (name.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Vui lòng nhập họ tên khách hàng")
            return
        }
        if (phone.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Vui lòng nhập số điện thoại")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isWriting = true, errorMessage = null, writeSuccess = false)

            val now = LocalDateTime.now()
            val suffix = now.format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
            val customerID = "KH$suffix"
            val cardID = "CARD$suffix"

            val cardResult = withContext(Dispatchers.IO) {
                try {
                    nfc.connectAndVerifyAdminPINEncrypted(adminPin = "9999").getOrElse { error ->
                        return@withContext Result.failure<String>(
                            Exception(error.message ?: "Không kết nối/xác thực được thẻ.")
                        )
                    }
                    nfc.setCustomerID(customerID)
                    val writeOk = nfc.writeCustomerInfo(customerID, cardID, name, dob, phone)
                    if (!writeOk) {
                        nfc.disconnect()
                        return@withContext Result.failure<String>(Exception("Ghi thông tin lên thẻ thất bại."))
                    }
                    val publicKeyPem = nfc.generateRSAKeyPairAndGetPublicKeyPem().getOrElse { error ->
                        nfc.disconnect()
                        return@withContext Result.failure<String>(
                            Exception(error.message ?: "Tạo cặp khóa RSA thất bại.")
                        )
                    }
                    if (!nfc.setBalance(0)) {
                        nfc.disconnect()
                        return@withContext Result.failure<String>(Exception("Khởi tạo số dư trên thẻ thất bại."))
                    }
                    nfc.disconnect()
                    Result.success(publicKeyPem)
                } catch (e: Exception) {
                    nfc.disconnect()
                    Result.failure(e)
                }
            }

            cardResult.fold(
                onSuccess = { publicKeyPem ->
                    repo.directIssue(
                        DirectIssueRequest(
                            customerID = customerID,
                            cardID = cardID,
                            fullName = name,
                            dateOfBirth = dob.ifBlank { null },
                            phoneNumber = phone,
                            publicKey = publicKeyPem
                        )
                    ).fold(
                        onSuccess = {
                            _state.value = _state.value.copy(
                                isWriting = false,
                                writeSuccess = true,
                                issuedCardID = cardID,
                                successMessage = "Cấp thẻ thành công! Mã thẻ: $cardID"
                            )
                        },
                        onFailure = { e ->
                            _state.value = _state.value.copy(
                                isWriting = false,
                                errorMessage = "Ghi thẻ thành công nhưng lưu server thất bại: ${e.message}"
                            )
                        }
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isWriting = false, errorMessage = e.message)
                }
            )
        }
    }

    fun reset() { _state.value = IssueCardState() }
    fun clearMessages() { _state.value = _state.value.copy(successMessage = null, errorMessage = null) }
}
