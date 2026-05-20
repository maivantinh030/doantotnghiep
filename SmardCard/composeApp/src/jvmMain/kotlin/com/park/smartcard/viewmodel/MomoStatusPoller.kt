package com.park.smartcard.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.park.smartcard.data.model.TopUpResult

/**
 * Helper poll trạng thái MoMo top-up theo chu kỳ.
 *
 * Tách khỏi TopUpViewModel để VM chỉ cần `start(...)` rồi nhận `Event` về,
 * không phải tự quản lý `delay` / `Job` / merge URL.
 *
 * Caller tự cập nhật UI state khi nhận Event.
 */
class MomoStatusPoller(
    private val scope: CoroutineScope,
    private val getStatus: suspend (userId: String, orderId: String) -> Result<TopUpResult>,
    private val intervalMs: Long = 2_000L,
    private val maxAttempts: Int = 60
) {
    sealed interface Event {
        /** Vẫn đang chờ khách thanh toán. Payment đã merge link cũ + mới. */
        data class Pending(val payment: TopUpResult) : Event

        /** Thanh toán thành công. */
        data class Success(val payment: TopUpResult) : Event

        /** Thanh toán thất bại (failed/canceled/error). */
        data class Failed(val payment: TopUpResult) : Event

        /** Quá số lần poll tối đa — giao dịch vẫn đang chờ phía MoMo. */
        data object Timeout : Event

        /** Không gọi được API status (lỗi mạng, server). */
        data class StatusError(val message: String) : Event
    }

    private var pollingJob: Job? = null
    private var cachedPayment: TopUpResult? = null

    /**
     * Bắt đầu polling cho đến khi nhận terminal event (Success/Failed/Timeout)
     * hoặc `onEvent` trả `true` để dừng sớm.
     */
    fun start(
        userId: String,
        orderId: String,
        initialPayment: TopUpResult,
        onEvent: suspend (Event) -> Boolean
    ) {
        cancel()
        cachedPayment = initialPayment
        pollingJob = scope.launch {
            repeat(maxAttempts) {
                delay(intervalMs)
                val terminal = checkOnce(userId, orderId, onEvent)
                if (terminal) return@launch
            }
            onEvent(Event.Timeout)
        }
    }

    /** Check 1 lần thủ công (vd: user bấm "Kiểm tra lại"). Trả `true` nếu đã đạt terminal event. */
    suspend fun checkOnce(
        userId: String,
        orderId: String,
        onEvent: suspend (Event) -> Boolean
    ): Boolean {
        val result = getStatus(userId, orderId)
        return result.fold(
            onSuccess = { latest ->
                val merged = mergeMomoPaymentLinks(latest, cachedPayment)
                cachedPayment = merged
                val event = when (merged.status.uppercase()) {
                    "SUCCESS" -> Event.Success(merged)
                    "FAILED", "FAIL", "CANCELED", "CANCELLED", "ERROR" -> Event.Failed(merged)
                    else -> Event.Pending(merged)
                }
                val terminal = event is Event.Success || event is Event.Failed
                val stopRequested = onEvent(event)
                terminal || stopRequested
            },
            onFailure = { error ->
                onEvent(Event.StatusError(error.message ?: "Không thể kiểm tra trạng thái MoMo"))
                false
            }
        )
    }

    fun cancel() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /** Giữ qrCodeUrl từ payment cũ (link ban đầu) + payUrl mới nếu MoMo cập nhật. */
    private fun mergeMomoPaymentLinks(latest: TopUpResult, previous: TopUpResult?): TopUpResult {
        val qrCodeUrl = previous?.qrCodeUrl?.takeIf { it.isNotBlank() }
            ?: latest.qrCodeUrl?.takeIf { it.isNotBlank() }
        val payUrl = latest.payUrl?.takeIf { it.isNotBlank() }
            ?: previous?.payUrl?.takeIf { it.isNotBlank() }
        return latest.copy(qrCodeUrl = qrCodeUrl, payUrl = payUrl)
    }
}
