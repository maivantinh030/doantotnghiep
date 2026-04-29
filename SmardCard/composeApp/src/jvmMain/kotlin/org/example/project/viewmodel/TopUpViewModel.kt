package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.SmartCardManager
import org.example.project.data.model.CardDTO
import org.example.project.data.model.CustomerDTO
import org.example.project.data.model.TopUpResult
import org.example.project.data.repository.PendingGamePlayRepository
import org.example.project.data.repository.StaffRepository
import org.example.project.data.repository.isAuthError
import java.math.BigDecimal

enum class StaffTopUpMethod {
    CASH,
    MOMO
}

data class TopUpState(
    val isReading: Boolean = false,
    val isFetching: Boolean = false,
    val isTopingUp: Boolean = false,
    val isPollingMomo: Boolean = false,
    val selectedMethod: StaffTopUpMethod = StaffTopUpMethod.CASH,
    val scannedCard: CardDTO? = null,
    val customer: CustomerDTO? = null,
    val momoPayment: TopUpResult? = null,
    val momoStatusMessage: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class TopUpViewModel(private val repo: StaffRepository = StaffRepository()) : ViewModel() {

    private companion object {
        const val MOMO_POLL_INTERVAL_MS = 2_000L
        const val MOMO_MAX_POLL_ATTEMPTS = 60
    }

    private val _state = MutableStateFlow(TopUpState())
    val state: StateFlow<TopUpState> = _state

    private val nfc = SmartCardManager()
    private val pendingRepository = PendingGamePlayRepository()
    private var momoPollingJob: Job? = null

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

    fun selectMethod(method: StaffTopUpMethod) {
        if (_state.value.selectedMethod == method) return
        momoPollingJob?.cancel()
        _state.value = _state.value.copy(
            selectedMethod = method,
            isPollingMomo = false,
            momoPayment = null,
            momoStatusMessage = null,
            errorMessage = null,
            successMessage = null
        )
    }

    fun topUp(amount: String) {
        when (_state.value.selectedMethod) {
            StaffTopUpMethod.CASH -> topUpCash(amount)
            StaffTopUpMethod.MOMO -> createMomoTopUp(amount)
        }
    }

    private fun topUpCash(amount: String) {
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

                    val topUpResult = repo.topUpForCustomer(customer.userId, amount, "CASH").getOrElse { error ->
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

    private fun createMomoTopUp(amount: String) {
        val customer = _state.value.customer ?: return

        val topUpAmount = amount.toBigDecimalOrNull()
        if (topUpAmount == null || topUpAmount <= BigDecimal.ZERO) {
            _state.value = _state.value.copy(errorMessage = "Số tiền không hợp lệ")
            return
        }

        momoPollingJob?.cancel()
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isTopingUp = true,
                isPollingMomo = false,
                momoPayment = null,
                momoStatusMessage = null,
                errorMessage = null,
                successMessage = null
            )

            val flushResult = flushPendingTransactions()
            if (flushResult.isFailure) {
                _state.value = _state.value.copy(
                    isTopingUp = false,
                    errorMessage = flushResult.exceptionOrNull()?.message ?: "Không thể đồng bộ lượt chơi chờ."
                )
                return@launch
            }

            repo.topUpForCustomer(customer.userId, amount, "MOMO").fold(
                onSuccess = { payment ->
                    val orderId = payment.orderId
                    _state.value = _state.value.copy(
                        isTopingUp = false,
                        isPollingMomo = !orderId.isNullOrBlank(),
                        momoPayment = payment,
                        momoStatusMessage = "Đang chờ khách quét QR và thanh toán MoMo"
                    )

                    if (orderId.isNullOrBlank()) {
                        _state.value = _state.value.copy(
                            isPollingMomo = false,
                            errorMessage = "Không nhận được mã giao dịch MoMo"
                        )
                    } else {
                        startMomoPolling(customer.userId, orderId)
                    }
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(isTopingUp = false, errorMessage = error.message)
                }
            )
        }
    }

    fun checkMomoStatusNow() {
        val customer = _state.value.customer ?: return
        val orderId = _state.value.momoPayment?.orderId ?: return
        viewModelScope.launch {
            if (refreshMomoStatus(customer.userId, orderId)) {
                momoPollingJob?.cancel()
            }
        }
    }

    fun cancelMomoTopUp() {
        momoPollingJob?.cancel()
        _state.value = _state.value.copy(
            isPollingMomo = false,
            momoPayment = null,
            momoStatusMessage = null
        )
    }

    private fun startMomoPolling(userId: String, orderId: String) {
        momoPollingJob?.cancel()
        momoPollingJob = viewModelScope.launch {
            repeat(MOMO_MAX_POLL_ATTEMPTS) {
                delay(MOMO_POLL_INTERVAL_MS)
                if (refreshMomoStatus(userId, orderId)) return@launch
            }
            _state.value = _state.value.copy(
                isPollingMomo = false,
                momoStatusMessage = "Giao dịch vẫn đang chờ xử lý. Có thể bấm kiểm tra lại sau."
            )
        }
    }

    private suspend fun refreshMomoStatus(userId: String, orderId: String): Boolean {
        return repo.getTopUpStatus(userId, orderId).fold(
            onSuccess = { latest ->
                val payment = mergeMomoPaymentLinks(latest, _state.value.momoPayment)
                when (payment.status.uppercase()) {
                    "SUCCESS" -> {
                        syncCardAfterMomoSuccess(userId, payment)
                        true
                    }
                    "FAILED", "FAIL", "CANCELED", "CANCELLED", "ERROR" -> {
                        _state.value = _state.value.copy(
                            isPollingMomo = false,
                            momoPayment = payment,
                            momoStatusMessage = null,
                            errorMessage = "Giao dịch MoMo không thành công"
                        )
                        true
                    }
                    else -> {
                        _state.value = _state.value.copy(
                            isPollingMomo = true,
                            momoPayment = payment,
                            momoStatusMessage = "Đang chờ khách thanh toán MoMo"
                        )
                        false
                    }
                }
            },
            onFailure = { error ->
                _state.value = _state.value.copy(
                    momoStatusMessage = error.message ?: "Không thể kiểm tra trạng thái MoMo"
                )
                false
            }
        )
    }

    private suspend fun syncCardAfterMomoSuccess(userId: String, payment: TopUpResult) {
        val scannedCard = _state.value.scannedCard
        val latestCustomer = repo.getCustomerById(userId).getOrElse { error ->
            _state.value = _state.value.copy(
                isPollingMomo = false,
                momoPayment = payment,
                errorMessage = "MoMo đã thanh toán nhưng không tải được số dư mới: ${error.message}"
            )
            return
        }

        val updateResult = withContext(Dispatchers.IO) {
            try {
                if (scannedCard == null) {
                    return@withContext Result.failure<CustomerDTO>(Exception("Không còn thông tin thẻ vừa quẹt."))
                }

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

                val newBalance = latestCustomer.currentBalance.toBalanceInt()
                if (!nfc.setBalance(newBalance)) {
                    nfc.disconnect()
                    return@withContext Result.failure<CustomerDTO>(Exception("MoMo đã thanh toán nhưng cập nhật thẻ thất bại."))
                }

                nfc.disconnect()
                Result.success(latestCustomer)
            } catch (e: Exception) {
                nfc.disconnect()
                Result.failure(e)
            }
        }

        updateResult.fold(
            onSuccess = { updatedCustomer ->
                _state.value = _state.value.copy(
                    isPollingMomo = false,
                    momoPayment = null,
                    customer = updatedCustomer,
                    momoStatusMessage = null,
                    successMessage = "MoMo thanh toán thành công và đã cập nhật số dư trên thẻ"
                )
            },
            onFailure = { error ->
                _state.value = _state.value.copy(
                    isPollingMomo = false,
                    momoPayment = payment,
                    momoStatusMessage = null,
                    errorMessage = error.message
                )
            }
        )
    }

    private fun mergeMomoPaymentLinks(latest: TopUpResult, previous: TopUpResult?): TopUpResult {
        val qrCodeUrl = previous?.qrCodeUrl?.takeIf { it.isNotBlank() }
            ?: latest.qrCodeUrl?.takeIf { it.isNotBlank() }
        val payUrl = latest.payUrl?.takeIf { it.isNotBlank() }
            ?: previous?.payUrl?.takeIf { it.isNotBlank() }

        return latest.copy(qrCodeUrl = qrCodeUrl, payUrl = payUrl)
    }

    fun reset() {
        momoPollingJob?.cancel()
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

    override fun onCleared() {
        momoPollingJob?.cancel()
        super.onCleared()
    }
}

private fun String.toBalanceInt(): Int {
    return toBigDecimalOrNull()?.setScale(0)?.toInt() ?: 0
}
