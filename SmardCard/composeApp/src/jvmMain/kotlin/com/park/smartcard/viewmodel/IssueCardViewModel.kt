package com.park.smartcard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.park.smartcard.data.model.DirectIssueRequest
import com.park.smartcard.data.repository.CardRequestRepository
import com.park.smartcard.data.repository.NfcCardRepository
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
    private val cardRequestRepo: CardRequestRepository = CardRequestRepository(),
    private val nfcRepo: NfcCardRepository = NfcCardRepository()
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

            nfcRepo.issueNewCard(
                customerId = customerID,
                cardId = cardID,
                fullName = name,
                dateOfBirth = dob.ifBlank { null },
                phoneNumber = phone,
                initialBalance = 0
            ).fold(
                onSuccess = { publicKeyPem ->
                    cardRequestRepo.directIssue(
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
