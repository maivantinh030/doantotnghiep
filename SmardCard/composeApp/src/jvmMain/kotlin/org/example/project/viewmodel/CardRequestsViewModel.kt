package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.SmartCardManager
import org.example.project.data.model.CardRequestDTO
import org.example.project.data.repository.StaffRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class CardRequestsState(
    val isLoading: Boolean = false,
    val requests: List<CardRequestDTO> = emptyList(),
    val statusFilter: String = "PENDING",
    val selectedRequest: CardRequestDTO? = null,
    val showReviewDialog: Boolean = false,
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class CardRequestsViewModel(
    private val repo: StaffRepository = StaffRepository(),
    private val nfc: SmartCardManager = SmartCardManager()
) : ViewModel() {

    private val _state = MutableStateFlow(CardRequestsState())
    val state: StateFlow<CardRequestsState> = _state

    init { load() }

    fun load(status: String? = null) {
        val s = status ?: _state.value.statusFilter
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            repo.getCardRequests(s).fold(
                onSuccess = { _state.value = _state.value.copy(isLoading = false, requests = it, statusFilter = s) },
                onFailure = { _state.value = _state.value.copy(isLoading = false, errorMessage = it.message) }
            )
        }
    }

    fun review(approved: Boolean, note: String?) {
        val req = _state.value.selectedRequest ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
            if (!approved) {
                repo.reviewRequest(req.requestId, approved = false, note = note).fold(
                    onSuccess = {
                        _state.value = _state.value.copy(
                            isSubmitting = false,
                            showReviewDialog = false,
                            selectedRequest = null,
                            successMessage = "Tu choi yeu cau thanh cong"
                        )
                        load()
                    },
                    onFailure = { _state.value = _state.value.copy(isSubmitting = false, errorMessage = it.message) }
                )
                return@launch
            }

            issueCardForRequest(req, closeDialog = true)
        }
    }

    fun issueApprovedRequest(req: CardRequestDTO) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
            issueCardForRequest(req, closeDialog = false)
        }
    }

    fun openReviewDialog(req: CardRequestDTO) { _state.value = _state.value.copy(showReviewDialog = true, selectedRequest = req) }
    fun closeReviewDialog() { _state.value = _state.value.copy(showReviewDialog = false, selectedRequest = null) }
    fun clearMessages() { _state.value = _state.value.copy(successMessage = null, errorMessage = null) }

    private suspend fun issueCardForRequest(req: CardRequestDTO, closeDialog: Boolean) {
        val customer = repo.getCustomerById(req.userId).getOrElse { error ->
            _state.value = _state.value.copy(isSubmitting = false, errorMessage = error.message)
            return
        }

        val fullName = customer.fullName?.trim().takeUnless { it.isNullOrBlank() } ?: run {
            _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = "Nguoi dung chua co ho ten de ghi len the"
            )
            return
        }
        val phoneNumber = customer.phoneNumber.trim()
        if (phoneNumber.isBlank()) {
            _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = "Nguoi dung chua co so dien thoai hop le"
            )
            return
        }

        val cardId = buildCardId()
        val publicKeyPem = withContext(Dispatchers.IO) {
            try {
                nfc.connectAndVerifyAdminPINEncrypted(adminPin = "9999").getOrElse { error ->
                    return@withContext Result.failure<String>(
                        Exception(error.message ?: "Khong ket noi/xac thuc duoc the.")
                    )
                }

                nfc.setCustomerID(customer.userId)
                val writeOk = nfc.writeCustomerInfo(
                    customerID = customer.userId,
                    cardId = cardId,
                    name = fullName,
                    dateOfBirth = customer.dateOfBirth.orEmpty(),
                    phoneNumber = phoneNumber
                )
                if (!writeOk) {
                    nfc.disconnect()
                    return@withContext Result.failure<String>(Exception("Ghi thong tin nguoi dung len the that bai."))
                }

                val key = nfc.generateRSAKeyPairAndGetPublicKeyPem().getOrElse { error ->
                    nfc.disconnect()
                    return@withContext Result.failure<String>(
                        Exception(error.message ?: "Tao cap khoa RSA tren the that bai.")
                    )
                }

                val initialBalance = customer.currentBalance.toBigDecimalOrNull()
                    ?.setScale(0)
                    ?.toInt()
                    ?: 0
                if (!nfc.setBalance(initialBalance)) {
                    nfc.disconnect()
                    return@withContext Result.failure<String>(Exception("Khoi tao so du tren the that bai."))
                }

                nfc.disconnect()

                Result.success(key)
            } catch (e: Exception) {
                nfc.disconnect()
                Result.failure(e)
            }
        }.getOrElse { error ->
            _state.value = _state.value.copy(isSubmitting = false, errorMessage = error.message)
            return
        }

        repo.issueCardRequest(req.requestId, cardId, publicKeyPem).fold(
            onSuccess = {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    showReviewDialog = if (closeDialog) false else _state.value.showReviewDialog,
                    selectedRequest = if (closeDialog) null else _state.value.selectedRequest,
                    successMessage = "Cap the thanh cong cho ${fullName}. Ma the: $cardId"
                )
                load()
            },
            onFailure = {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    errorMessage = it.message
                )
            }
        )
    }

    private fun buildCardId(): String {
        val suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
        return "CARD$suffix"
    }
}
