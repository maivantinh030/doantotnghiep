package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.repository.WalletRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class TopUpTrackingStatus {
    IDLE,
    LOADING,
    WAITING,
    SUCCESS,
    FAILED,
    TIMEOUT
}

data class TopUpTrackingState(
    val status: TopUpTrackingStatus = TopUpTrackingStatus.IDLE,
    val payment: PaymentRecordDTO? = null,
    val orderId: String? = null,
    val amount: String? = null,
    val message: String? = null
)

class WalletViewModel(private val walletRepository: WalletRepository) : ViewModel() {

    private companion object {
        const val POLL_INTERVAL_MS = 2_000L
        const val MAX_POLL_ATTEMPTS = 60
    }

    private val _balanceState = MutableStateFlow<Resource<WalletBalanceDTO>?>(null)
    val balanceState: StateFlow<Resource<WalletBalanceDTO>?> = _balanceState

    private val _topUpState = MutableStateFlow<Resource<PaymentRecordDTO>?>(null)
    val topUpState: StateFlow<Resource<PaymentRecordDTO>?> = _topUpState

    private val _topUpTrackingState = MutableStateFlow(TopUpTrackingState())
    val topUpTrackingState: StateFlow<TopUpTrackingState> = _topUpTrackingState

    private val _transactionsState = MutableStateFlow<Resource<PaginatedData<TransactionDTO>>?>(null)
    val transactionsState: StateFlow<Resource<PaginatedData<TransactionDTO>>?> = _transactionsState

    private val _paymentsState = MutableStateFlow<Resource<PaginatedData<PaymentRecordDTO>>?>(null)
    val paymentsState: StateFlow<Resource<PaginatedData<PaymentRecordDTO>>?> = _paymentsState

    private var topUpPollingJob: Job? = null

    fun loadBalance() {
        viewModelScope.launch {
            _balanceState.value = Resource.Loading
            _balanceState.value = walletRepository.getBalance()
        }
    }

    fun topUp(amount: String, method: String) {
        topUpPollingJob?.cancel()
        viewModelScope.launch {
            _topUpState.value = Resource.Loading
            _topUpTrackingState.value = TopUpTrackingState(
                status = TopUpTrackingStatus.LOADING,
                amount = amount,
                message = "Đang tạo giao dịch MoMo"
            )

            val result = walletRepository.topUp(amount, method)
            _topUpState.value = result

            when (result) {
                is Resource.Success -> {
                    val payment = result.data
                    val orderId = payment.orderId
                    _topUpTrackingState.value = TopUpTrackingState(
                        status = TopUpTrackingStatus.WAITING,
                        payment = payment,
                        orderId = orderId,
                        amount = payment.amount,
                        message = "Đang chờ thanh toán MoMo"
                    )

                    if (orderId.isNullOrBlank()) {
                        _topUpTrackingState.value = _topUpTrackingState.value.copy(
                            status = TopUpTrackingStatus.FAILED,
                            message = "Không nhận được mã giao dịch từ máy chủ"
                        )
                    } else {
                        startTopUpPolling(orderId)
                    }
                }

                is Resource.Error -> {
                    _topUpTrackingState.value = TopUpTrackingState(
                        status = TopUpTrackingStatus.FAILED,
                        amount = amount,
                        message = result.message
                    )
                }

                Resource.Loading -> Unit
            }
        }
    }

    fun checkCurrentTopUpStatus() {
        val current = _topUpTrackingState.value
        val orderId = current.orderId ?: current.payment?.orderId ?: return
        viewModelScope.launch {
            if (refreshTopUpStatus(orderId)) {
                topUpPollingJob?.cancel()
            }
        }
    }

    fun loadTransactions(page: Int = 1, size: Int = 10, type: String? = null) {
        viewModelScope.launch {
            _transactionsState.value = Resource.Loading
            _transactionsState.value = walletRepository.getTransactions(page, size, type)
        }
    }

    fun loadPayments(page: Int = 1, size: Int = 10) {
        viewModelScope.launch {
            _paymentsState.value = Resource.Loading
            _paymentsState.value = walletRepository.getPayments(page, size)
        }
    }

    fun resetTopUpState() {
        topUpPollingJob?.cancel()
        _topUpState.value = null
        _topUpTrackingState.value = TopUpTrackingState()
    }

    private fun startTopUpPolling(orderId: String) {
        topUpPollingJob?.cancel()
        topUpPollingJob = viewModelScope.launch {
            repeat(MAX_POLL_ATTEMPTS) {
                delay(POLL_INTERVAL_MS)
                if (refreshTopUpStatus(orderId)) return@launch
            }

            val current = _topUpTrackingState.value
            if (current.status == TopUpTrackingStatus.WAITING) {
                _topUpTrackingState.value = current.copy(
                    status = TopUpTrackingStatus.TIMEOUT,
                    message = "Giao dịch vẫn đang chờ xử lý. Bạn có thể kiểm tra lại sau."
                )
            }
        }
    }

    private suspend fun refreshTopUpStatus(orderId: String): Boolean {
        return when (val result = walletRepository.getTopUpStatus(orderId)) {
            is Resource.Success -> {
                val payment = mergeTopUpPaymentLinks(
                    latest = result.data,
                    previous = _topUpTrackingState.value.payment
                )
                _topUpState.value = Resource.Success(payment)

                when (payment.status.uppercase()) {
                    "SUCCESS" -> {
                        _topUpTrackingState.value = TopUpTrackingState(
                            status = TopUpTrackingStatus.SUCCESS,
                            payment = payment,
                            orderId = payment.orderId ?: orderId,
                            amount = payment.amount,
                            message = "Nạp tiền thành công"
                        )
                        loadBalance()
                        loadPayments()
                        true
                    }

                    "FAILED", "FAIL", "CANCELED", "CANCELLED", "ERROR" -> {
                        _topUpTrackingState.value = TopUpTrackingState(
                            status = TopUpTrackingStatus.FAILED,
                            payment = payment,
                            orderId = payment.orderId ?: orderId,
                            amount = payment.amount,
                            message = "Giao dịch MoMo không thành công"
                        )
                        true
                    }

                    else -> {
                        _topUpTrackingState.value = TopUpTrackingState(
                            status = TopUpTrackingStatus.WAITING,
                            payment = payment,
                            orderId = payment.orderId ?: orderId,
                            amount = payment.amount,
                            message = "Đang chờ thanh toán MoMo"
                        )
                        false
                    }
                }
            }

            is Resource.Error -> {
                val current = _topUpTrackingState.value
                _topUpTrackingState.value = current.copy(
                    status = TopUpTrackingStatus.WAITING,
                    message = result.message
                )
                false
            }

            Resource.Loading -> false
        }
    }

    private fun mergeTopUpPaymentLinks(
        latest: PaymentRecordDTO,
        previous: PaymentRecordDTO?
    ): PaymentRecordDTO {
        val latestQrCodeUrl = latest.qrCodeUrl?.takeIf { it.isNotBlank() }
        val previousQrCodeUrl = previous?.qrCodeUrl?.takeIf { it.isNotBlank() }
        val latestPayUrl = latest.payUrl?.takeIf { it.isNotBlank() }
        val previousPayUrl = previous?.payUrl?.takeIf { it.isNotBlank() }

        val mergedPayUrl = latestPayUrl
            ?: previousPayUrl
            ?: latestQrCodeUrl?.takeIf { it.looksLikeMomoPayUrl() }

        val mergedQrCodeUrl = when {
            latestQrCodeUrl == null -> previousQrCodeUrl
            latestQrCodeUrl.looksLikeMomoPayUrl() -> previousQrCodeUrl ?: latestQrCodeUrl
            else -> latestQrCodeUrl
        }

        return latest.copy(
            payUrl = mergedPayUrl,
            qrCodeUrl = mergedQrCodeUrl
        )
    }

    private fun String.looksLikeMomoPayUrl(): Boolean {
        return contains("payment.momo.vn", ignoreCase = true) ||
            contains("test-payment.momo.vn", ignoreCase = true)
    }

    override fun onCleared() {
        topUpPollingJob?.cancel()
        super.onCleared()
    }

    class Factory(private val repository: WalletRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WalletViewModel(repository) as T
        }
    }
}
