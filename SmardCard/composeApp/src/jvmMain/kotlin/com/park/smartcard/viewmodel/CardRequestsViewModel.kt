package com.park.smartcard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.park.smartcard.data.model.CardRequestDTO
import com.park.smartcard.data.repository.CardRequestRepository
import com.park.smartcard.data.repository.CustomerRepository
import com.park.smartcard.data.repository.NfcCardRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class CardRequestsState(
    val isLoading: Boolean = false,
    val requests: List<CardRequestDTO> = emptyList(),
    val statusFilter: String = "PENDING",
    val searchQuery: String = "",
    val selectedRequest: CardRequestDTO? = null,
    val showReviewDialog: Boolean = false,
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class CardRequestsViewModel(
    private val cardRequestRepo: CardRequestRepository = CardRequestRepository(),
    private val customerRepo: CustomerRepository = CustomerRepository(),
    private val nfcRepo: NfcCardRepository = NfcCardRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(CardRequestsState())
    val state: StateFlow<CardRequestsState> = _state

    init { load() }

    fun load(status: String? = null) {
        val s = status ?: _state.value.statusFilter
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            cardRequestRepo.getCardRequests(s).fold(
                onSuccess = { requests ->
                    val requesterCache = mutableMapOf<String, com.park.smartcard.data.model.CustomerDTO?>()
                    val hydratedRequests = requests.map { req ->
                        if (req.requester != null) {
                            req
                        } else {
                            val requester = requesterCache.getOrPut(req.userId) {
                                customerRepo.getCustomerById(req.userId).getOrNull()
                            }
                            req.copy(requester = requester)
                        }
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        requests = hydratedRequests,
                        statusFilter = s
                    )
                },
                onFailure = { _state.value = _state.value.copy(isLoading = false, errorMessage = it.message) }
            )
        }
    }

    fun review(approved: Boolean, note: String?) {
        val req = _state.value.selectedRequest ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
            if (!approved) {
                cardRequestRepo.reviewRequest(req.requestId, approved = false, note = note).fold(
                    onSuccess = {
                        _state.value = _state.value.copy(
                            isSubmitting = false,
                            showReviewDialog = false,
                            selectedRequest = null,
                            successMessage = "Từ chối yêu cầu thành công"
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

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun openReviewDialog(req: CardRequestDTO) { _state.value = _state.value.copy(showReviewDialog = true, selectedRequest = req) }
    fun closeReviewDialog() { _state.value = _state.value.copy(showReviewDialog = false, selectedRequest = null) }
    fun clearMessages() { _state.value = _state.value.copy(successMessage = null, errorMessage = null) }

    private suspend fun issueCardForRequest(req: CardRequestDTO, closeDialog: Boolean) {
        val customer = customerRepo.getCustomerById(req.userId).getOrElse { error ->
            _state.value = _state.value.copy(isSubmitting = false, errorMessage = error.message)
            return
        }

        val fullName = customer.fullName?.trim().takeUnless { it.isNullOrBlank() } ?: run {
            _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = "Người dùng chưa có họ tên để ghi lên thẻ"
            )
            return
        }
        val phoneNumber = customer.phoneNumber.trim()
        if (phoneNumber.isBlank()) {
            _state.value = _state.value.copy(
                isSubmitting = false,
                errorMessage = "Người dùng chưa có số điện thoại hợp lệ"
            )
            return
        }

        val cardId = buildCardId()
        val initialBalance = customer.currentBalance.toBigDecimalOrNull()
            ?.setScale(0)
            ?.toInt()
            ?: 0
        val publicKeyPem = nfcRepo.issueNewCard(
            customerId = customer.userId,
            cardId = cardId,
            fullName = fullName,
            dateOfBirth = customer.dateOfBirth,
            phoneNumber = phoneNumber,
            initialBalance = initialBalance
        ).getOrElse { error ->
            _state.value = _state.value.copy(isSubmitting = false, errorMessage = error.message)
            return
        }

        cardRequestRepo.issueCardRequest(req.requestId, cardId, publicKeyPem).fold(
            onSuccess = {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    showReviewDialog = if (closeDialog) false else _state.value.showReviewDialog,
                    selectedRequest = if (closeDialog) null else _state.value.selectedRequest,
                    successMessage = "Cấp thẻ thành công cho ${fullName}. Mã thẻ: $cardId"
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
