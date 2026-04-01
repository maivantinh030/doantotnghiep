package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.SmartCardManager
import org.example.project.data.model.CardDTO
import org.example.project.data.model.ReturnSummary
import org.example.project.data.repository.StaffRepository

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

class ReturnCardViewModel(private val repo: StaffRepository = StaffRepository()) : ViewModel() {

    private val _state = MutableStateFlow(ReturnCardState())
    val state: StateFlow<ReturnCardState> = _state

    private val nfc = SmartCardManager()

    fun scanCard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isReading = true, errorMessage = null)

            val connectResult = withContext(Dispatchers.IO) {
                nfc.connectAndVerifyAdminPINEncrypted(adminPin = "9999")
            }
            if (connectResult.isFailure) {
                _state.value = _state.value.copy(
                    isReading = false,
                    errorMessage = connectResult.exceptionOrNull()?.message
                        ?: "Khong ket noi/xac thuc duoc the."
                )
                return@launch
            }

            val cardInfo = withContext(Dispatchers.IO) { nfc.readCustomerInfo() }
            withContext(Dispatchers.IO) { nfc.disconnect() }

            val cardID = cardInfo["cardUUID"]
            if (cardID.isNullOrBlank()) {
                _state.value = _state.value.copy(
                    isReading = false,
                    errorMessage = "Khong doc duoc thong tin the."
                )
                return@launch
            }

            onCardDetected(cardID)
        }
    }

    private suspend fun onCardDetected(cardId: String) {
        _state.value = _state.value.copy(isReading = false, isFetching = true)
        repo.lookupCardByCardId(cardId).fold(
            onSuccess = { card ->
                if (card.status == "AVAILABLE") {
                    _state.value = _state.value.copy(
                        isFetching = false,
                        errorMessage = "The nay chua duoc cap cho khach nao"
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
            repo.returnCard(cardId).fold(
                onSuccess = { summary ->
                    _state.value = _state.value.copy(
                        isReturning = false,
                        scannedCard = null,
                        returnSummary = summary,
                        successMessage = "Tra the thanh cong!\n• Hoan balance: ${summary.refundedBalance} VND\n• Hoan coc: ${summary.refundedDeposit} VND"
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
