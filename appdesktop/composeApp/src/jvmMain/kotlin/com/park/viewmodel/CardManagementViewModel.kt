package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.BlockCardRequest
import com.park.data.model.CardDTO
import com.park.data.model.CardRequestDTO
import com.park.data.model.IssueCardRequest
import com.park.data.model.RegisterCardRequest
import com.park.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CardManagementState(
    val availableCards: List<CardDTO> = emptyList(),
    val cardRequests: List<CardRequestDTO> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val showRegisterDialog: Boolean = false,
    val showIssueDialog: Boolean = false,
    val showReturnDialog: Boolean = false,
    val showBlockDialog: Boolean = false,
    val showReviewDialog: Boolean = false,
    val selectedCard: CardDTO? = null,
    val selectedRequest: CardRequestDTO? = null,
    val requestStatusFilter: String = "PENDING"
)

class CardManagementViewModel(
    private val repository: CardRepository = CardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardManagementState())
    val uiState: StateFlow<CardManagementState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        loadAvailableCards()
        loadCardRequests()
    }

    fun loadAvailableCards() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAvailableCards().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        availableCards = it,
                        isLoading = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = it.message,
                        isError = true
                    )
                }
            )
        }
    }

    fun loadCardRequests(status: String? = null) {
        val selectedStatus = status ?: _uiState.value.requestStatusFilter
        viewModelScope.launch {
            repository.getCardRequests(selectedStatus).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        cardRequests = it,
                        requestStatusFilter = selectedStatus
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(message = it.message, isError = true)
                }
            )
        }
    }

    fun registerCard(cardId: String, cardName: String?) {
        if (cardId.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Vui lòng nhập Card ID", isError = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.registerCard(
                RegisterCardRequest(
                    cardId = cardId.trim(),
                    cardName = cardName?.takeIf { it.isNotBlank() }
                )
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showRegisterDialog = false,
                        message = "Đăng ký thẻ thành công: ${it.cardId}",
                        isError = false
                    )
                    loadAvailableCards()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = it.message,
                        isError = true
                    )
                }
            )
        }
    }

    fun issueCard(cardId: String, userId: String, cardName: String?, depositAmount: String) {
        if (userId.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Vui lòng nhập User ID", isError = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.issueCard(
                IssueCardRequest(
                    cardId = cardId,
                    userId = userId.trim(),
                    cardName = cardName?.takeIf { it.isNotBlank() },
                    depositAmount = depositAmount.ifBlank { "0" }
                )
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showIssueDialog = false,
                        selectedCard = null,
                        message = "Phát hành thẻ thành công cho user ${it.userId}",
                        isError = false
                    )
                    loadAvailableCards()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = it.message,
                        isError = true
                    )
                }
            )
        }
    }

    fun returnCard(cardId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.returnCard(cardId).fold(
                onSuccess = { summary ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showReturnDialog = false,
                        selectedCard = null,
                        message = "Trả thẻ thành công. Hoàn số dư: ${summary.refundedBalance}, hoàn cọc: ${summary.refundedDeposit}",
                        isError = false
                    )
                    loadAvailableCards()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = it.message,
                        isError = true
                    )
                }
            )
        }
    }

    fun blockCard(cardId: String, reason: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.blockCard(cardId, reason).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showBlockDialog = false,
                        selectedCard = null,
                        message = "Khóa thẻ thành công",
                        isError = false
                    )
                    loadAvailableCards()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = it.message,
                        isError = true
                    )
                }
            )
        }
    }

    fun reviewCardRequest(requestId: String, approved: Boolean, note: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.reviewCardRequest(requestId, approved, note).fold(
                onSuccess = {
                    val action = if (approved) "Hoàn thành" else "Từ chối"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showReviewDialog = false,
                        selectedRequest = null,
                        message = "$action yêu cầu thành công",
                        isError = false
                    )
                    loadCardRequests()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = it.message,
                        isError = true
                    )
                }
            )
        }
    }

    fun completeCardRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.completeCardRequest(requestId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Đánh dấu hoàn thành yêu cầu",
                        isError = false
                    )
                    loadCardRequests()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = it.message,
                        isError = true
                    )
                }
            )
        }
    }

    fun openRegisterDialog() { _uiState.value = _uiState.value.copy(showRegisterDialog = true) }
    fun closeRegisterDialog() { _uiState.value = _uiState.value.copy(showRegisterDialog = false) }

    fun openIssueDialog(card: CardDTO) { _uiState.value = _uiState.value.copy(showIssueDialog = true, selectedCard = card) }
    fun closeIssueDialog() { _uiState.value = _uiState.value.copy(showIssueDialog = false, selectedCard = null) }

    fun openReturnDialog(card: CardDTO) { _uiState.value = _uiState.value.copy(showReturnDialog = true, selectedCard = card) }
    fun closeReturnDialog() { _uiState.value = _uiState.value.copy(showReturnDialog = false, selectedCard = null) }

    fun openBlockDialog(card: CardDTO) { _uiState.value = _uiState.value.copy(showBlockDialog = true, selectedCard = card) }
    fun closeBlockDialog() { _uiState.value = _uiState.value.copy(showBlockDialog = false, selectedCard = null) }

    fun openReviewDialog(req: CardRequestDTO) { _uiState.value = _uiState.value.copy(showReviewDialog = true, selectedRequest = req) }
    fun closeReviewDialog() { _uiState.value = _uiState.value.copy(showReviewDialog = false, selectedRequest = null) }

    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null, isError = false) }
}
