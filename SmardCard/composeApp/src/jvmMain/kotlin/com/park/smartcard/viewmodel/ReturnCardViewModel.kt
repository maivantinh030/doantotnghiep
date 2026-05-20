package com.park.smartcard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.park.smartcard.data.model.CardDTO
import com.park.smartcard.data.model.ReturnSummary
import com.park.smartcard.data.repository.CardRepository
import com.park.smartcard.data.repository.NfcCardRepository

data class ReturnCardState(
    val isReading: Boolean = false,
    val isFetching: Boolean = false,
    val isReturning: Boolean = false,
    val scannedCard: CardDTO? = null,
    val returnSummary: ReturnSummary? = null,
    val showConfirmDialog: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class ReturnCardViewModel(
    private val cardRepo: CardRepository = CardRepository(),
    private val nfcRepo: NfcCardRepository = NfcCardRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ReturnCardState())
    val state: StateFlow<ReturnCardState> = _state

    fun scanCard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isReading = true, errorMessage = null)

            nfcRepo.readCardId().fold(
                onSuccess = { cardId -> onCardDetected(cardId) },
                onFailure = { e ->
                    _state.value = _state.value.copy(isReading = false, errorMessage = e.message)
                }
            )
        }
    }

    private suspend fun onCardDetected(cardId: String) {
        _state.value = _state.value.copy(isReading = false, isFetching = true)
        cardRepo.lookupCardByCardId(cardId).fold(
            onSuccess = { card ->
                if (card.status == "AVAILABLE") {
                    _state.value = _state.value.copy(
                        isFetching = false,
                        errorMessage = "Thẻ này chưa được cấp cho khách nào"
                    )
                } else {
                    _state.value = _state.value.copy(
                        isFetching = false,
                        scannedCard = card,
                        showConfirmDialog = true
                    )
                }
            },
            onFailure = { e ->
                _state.value = _state.value.copy(isFetching = false, errorMessage = e.message)
            }
        )
    }

    fun confirmReturn() {
        val cardId = _state.value.scannedCard?.cardId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isReturning = true, showConfirmDialog = false, errorMessage = null)
            cardRepo.returnCard(cardId).fold(
                onSuccess = { summary ->
                    _state.value = _state.value.copy(
                        isReturning = false,
                        scannedCard = null,
                        returnSummary = summary,
                        successMessage = "Trả thẻ thành công!\n• Hoàn số dư: ${summary.refundedBalance} VND\n• Hoàn cọc: ${summary.refundedDeposit} VND"
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isReturning = false, errorMessage = e.message)
                }
            )
        }
    }

    fun cancelConfirm() {
        _state.value = _state.value.copy(showConfirmDialog = false, scannedCard = null)
    }

    fun reset() {
        _state.value = ReturnCardState()
    }

    fun clearMessages() {
        _state.value = _state.value.copy(successMessage = null, errorMessage = null, returnSummary = null)
    }
}
