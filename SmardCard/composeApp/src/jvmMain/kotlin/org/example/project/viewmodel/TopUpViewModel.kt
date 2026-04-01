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
import org.example.project.data.model.CustomerDTO
import org.example.project.data.repository.StaffRepository

data class TopUpState(
    val isReading: Boolean = false,
    val isFetching: Boolean = false,
    val isTopingUp: Boolean = false,
    val scannedCard: CardDTO? = null,
    val customer: CustomerDTO? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class TopUpViewModel(private val repo: StaffRepository = StaffRepository()) : ViewModel() {

    private val _state = MutableStateFlow(TopUpState())
    val state: StateFlow<TopUpState> = _state

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
                val userId = card.userId
                if (userId == null) {
                    _state.value = _state.value.copy(
                        isFetching = false,
                        errorMessage = "The chua lien ket voi tai khoan nao"
                    )
                    return
                }
                repo.getCustomerById(userId).fold(
                    onSuccess = { customer ->
                        _state.value = _state.value.copy(
                            isFetching = false,
                            scannedCard = card,
                            customer = customer
                        )
                    },
                    onFailure = { e ->
                        _state.value = _state.value.copy(isFetching = false, errorMessage = e.message)
                    }
                )
            },
            onFailure = { e ->
                _state.value = _state.value.copy(isFetching = false, errorMessage = e.message)
            }
        )
    }

    fun topUp(amount: String) {
        val userId = _state.value.customer?.userId ?: return
        if (amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
            _state.value = _state.value.copy(errorMessage = "So tien khong hop le")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isTopingUp = true, errorMessage = null)
            repo.topUpForCustomer(userId, amount).fold(
                onSuccess = { result ->
                    val old = _state.value.customer?.currentBalance?.toBigDecimalOrNull()
                        ?: java.math.BigDecimal.ZERO
                    val added = amount.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
                    _state.value = _state.value.copy(
                        isTopingUp = false,
                        customer = _state.value.customer?.copy(currentBalance = old.add(added).toString()),
                        successMessage = "Nap tien thanh cong: +${result.amount} VND"
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isTopingUp = false, errorMessage = e.message)
                }
            )
        }
    }

    fun reset() {
        _state.value = TopUpState()
    }

    fun clearMessages() {
        _state.value = _state.value.copy(successMessage = null, errorMessage = null)
    }
}
