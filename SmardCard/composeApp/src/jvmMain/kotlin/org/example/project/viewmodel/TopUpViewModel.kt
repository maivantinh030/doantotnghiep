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
import org.example.project.data.repository.PendingGamePlayRepository
import org.example.project.data.repository.StaffRepository
import org.example.project.data.repository.isAuthError
import java.math.BigDecimal

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
    private val pendingRepository = PendingGamePlayRepository()

    fun scanCard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isReading = true, errorMessage = null)

            val flushResult = flushPendingTransactions()
            if (flushResult.isFailure) {
                _state.value = _state.value.copy(
                    isReading = false,
                    errorMessage = flushResult.exceptionOrNull()?.message ?: "Không thể đồng bộ lượt chơi chờ."
                )
                return@launch
            }

            val cardId = withContext(Dispatchers.IO) {
                try {
                    nfc.connectAndVerifyAdminPINEncrypted(adminPin = "9999").getOrElse { error ->
                        return@withContext Result.failure<String>(
                            Exception(error.message ?: "Không kết nối/xác thực được thẻ.")
                        )
                    }
                    val cardInfo = nfc.readCustomerInfo()
                    val detectedCardId = cardInfo["cardUUID"]
                    nfc.disconnect()
                    if (detectedCardId.isNullOrBlank()) {
                        Result.failure(Exception("Không đọc được thông tin thẻ."))
                    } else {
                        Result.success(detectedCardId)
                    }
                } catch (e: Exception) {
                    nfc.disconnect()
                    Result.failure(e)
                }
            }

            cardId.fold(
                onSuccess = { onCardDetected(it) },
                onFailure = {
                    _state.value = _state.value.copy(isReading = false, errorMessage = it.message)
                }
            )
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
                        errorMessage = "Thẻ chưa liên kết với tài khoản nào"
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
        val scannedCard = _state.value.scannedCard ?: return
        val customer = _state.value.customer ?: return

        val topUpAmount = amount.toBigDecimalOrNull()
        if (topUpAmount == null || topUpAmount <= BigDecimal.ZERO) {
            _state.value = _state.value.copy(errorMessage = "Số tiền không hợp lệ")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isTopingUp = true, errorMessage = null)

            val flushResult = flushPendingTransactions()
            if (flushResult.isFailure) {
                _state.value = _state.value.copy(
                    isTopingUp = false,
                    errorMessage = flushResult.exceptionOrNull()?.message ?: "Không thể đồng bộ lượt chơi chờ."
                )
                return@launch
            }

            val latestCustomer = repo.getCustomerById(customer.userId).getOrElse { error ->
                _state.value = _state.value.copy(isTopingUp = false, errorMessage = error.message)
                return@launch
            }

            val updateResult = withContext(Dispatchers.IO) {
                try {
                    nfc.connectAndVerifyAdminPINEncrypted(adminPin = "9999").getOrElse { error ->
                        return@withContext Result.failure<CustomerDTO>(
                            Exception(error.message ?: "Không kết nối/xác thực được thẻ.")
                        )
                    }

                    val cardInfo = nfc.readCustomerInfo()
                    val detectedCardId = cardInfo["cardUUID"]?.trim().orEmpty()
                    if (detectedCardId.isBlank()) {
                        nfc.disconnect()
                        return@withContext Result.failure<CustomerDTO>(Exception("Không đọc được cardId trên thẻ."))
                    }
                    if (!detectedCardId.equals(scannedCard.cardId, ignoreCase = true)) {
                        nfc.disconnect()
                        return@withContext Result.failure<CustomerDTO>(Exception("Thẻ đang đặt không đúng với thẻ vừa quẹt."))
                    }

                    val serverBalanceBefore = latestCustomer.currentBalance.toBalanceInt()
                    if (!nfc.setBalance(serverBalanceBefore)) {
                        nfc.disconnect()
                        return@withContext Result.failure<CustomerDTO>(Exception("Không đồng bộ được số dư server xuống thẻ."))
                    }

                    val topUpResult = repo.topUpForCustomer(customer.userId, amount).getOrElse { error ->
                        nfc.disconnect()
                        return@withContext Result.failure<CustomerDTO>(error)
                    }

                    val newBalance = topUpResult.currentBalanceAfter?.toBalanceInt()
                        ?: serverBalanceBefore + topUpAmount.toInt()

                    if (!nfc.setBalance(newBalance)) {
                        nfc.disconnect()
                        return@withContext Result.failure<CustomerDTO>(Exception("Nạp tiền server thành công nhưng cập nhật thẻ thất bại."))
                    }

                    nfc.disconnect()
                    Result.success(latestCustomer.copy(currentBalance = newBalance.toString()))
                } catch (e: Exception) {
                    nfc.disconnect()
                    Result.failure(e)
                }
            }

            updateResult.fold(
                onSuccess = { updatedCustomer ->
                    _state.value = _state.value.copy(
                        isTopingUp = false,
                        customer = updatedCustomer,
                        successMessage = "Nạp tiền thành công và đã cập nhật số dư trên thẻ"
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(isTopingUp = false, errorMessage = error.message)
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

    private suspend fun flushPendingTransactions(): Result<Unit> {
        val result = pendingRepository.flush { play -> repo.syncPendingGamePlay(play) }
        if (result.failure != null) {
            val error = result.failure
            val prefix = if (result.syncedCount > 0) {
                "Đã đồng bộ ${result.syncedCount} giao dịch, "
            } else {
                ""
            }
            val message = when {
                error.isAuthError() -> prefix + "phiên đăng nhập không còn hợp lệ."
                else -> prefix + (error?.message ?: "không thể đồng bộ giao dịch chờ.")
            }
            return Result.failure(Exception(message, error))
        }
        return Result.success(Unit)
    }
}

private fun String.toBalanceInt(): Int {
    return toBigDecimalOrNull()?.setScale(0)?.toInt() ?: 0
}
