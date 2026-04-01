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
            _uiState.value = _uiState.value.copy(message = "Vui long nhap Card ID", isError = true)
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
                        message = "Dang ky the thanh cong: ${it.cardId}",
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
            _uiState.value = _uiState.value.copy(message = "Vui long nhap User ID", isError = true)
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
                        message = "Phat hanh the thanh cong cho user ${it.userId}",
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
                onSuccess = { data ->
                    val balance = data["refundedBalance"] ?: "0"
                    val deposit = data["refundedDeposit"] ?: "0"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showReturnDialog = false,
                        selectedCard = null,
                        message = "Tra the thanh cong. Hoan balance: $balance, hoan coc: $deposit",
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
                        message = "Khoa the thanh cong",
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
                    val action = if (approved) "Duyet" else "Tu choi"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showReviewDialog = false,
                        selectedRequest = null,
                        message = "$action yeu cau thanh cong",
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
                        message = "Danh dau hoan thanh yeu cau",
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
