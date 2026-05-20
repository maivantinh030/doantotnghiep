package com.park.smartcard.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.park.smartcard.SmartCardManager
import com.park.smartcard.config.NfcConstants

/**
 * Wrapper cho `SmartCardManager` — gom các sequence NFC thường dùng (connect → verify → read/write → disconnect)
 * thành use case rõ ràng để ViewModel chỉ gọi 1 method, không phải lặp lại try/catch + connect/disconnect.
 *
 * Mọi method đều chạy trên `Dispatchers.IO`, tự đảm bảo `disconnect()` được gọi kể cả khi exception.
 */
class NfcCardRepository(
    private val manager: SmartCardManager = SmartCardManager()
) {

    /**
     * Connect, verify admin PIN, đọc card ID từ thẻ rồi disconnect.
     */
    suspend fun readCardId(
        adminPin: String = NfcConstants.DEFAULT_ADMIN_PIN
    ): Result<String> = runOnNfc(adminPin) {
        val cardId = manager.readCustomerInfo()["cardUUID"]?.trim().orEmpty()
        if (cardId.isBlank()) error("Không đọc được thông tin thẻ.")
        cardId
    }

    /**
     * Cấp thẻ mới: ghi customer info + tạo RSA key pair + set số dư ban đầu.
     * Trả về public key PEM để lưu lên server.
     */
    suspend fun issueNewCard(
        customerId: String,
        cardId: String,
        fullName: String,
        dateOfBirth: String?,
        phoneNumber: String,
        initialBalance: Int = 0,
        adminPin: String = NfcConstants.DEFAULT_ADMIN_PIN
    ): Result<String> = runOnNfc(adminPin) {
        manager.setCustomerID(customerId)
        val writeOk = manager.writeCustomerInfo(
            customerID = customerId,
            cardId = cardId,
            name = fullName,
            dateOfBirth = dateOfBirth.orEmpty(),
            phoneNumber = phoneNumber
        )
        if (!writeOk) error("Ghi thông tin lên thẻ thất bại.")

        val publicKeyPem = manager.generateRSAKeyPairAndGetPublicKeyPem()
            .getOrElse { e -> error(e.message ?: "Tạo cặp khóa RSA thất bại.") }

        if (!manager.setBalance(initialBalance)) error("Khởi tạo số dư trên thẻ thất bại.")

        publicKeyPem
    }

    /**
     * Topup tiền mặt — orchestrate thẻ + server trong 1 transaction NFC:
     * 1. Read cardId, validate match expectedCardId.
     * 2. Đồng bộ số dư server hiện tại xuống thẻ trước khi nạp.
     * 3. Gọi `doServerTopup` để thực hiện HTTP nạp tiền, lấy số dư mới.
     * 4. Set số dư mới lên thẻ.
     *
     * Nếu bất kỳ bước nào fail, thẻ sẽ ở trạng thái cũ (chưa nạp). HTTP đã commit nhưng thẻ chưa cập nhật
     * sẽ được caller báo lỗi để retry/sync sau.
     *
     * @param doServerTopup lambda gọi HTTP topup, nhận về số dư mới của user (currentBalance sau khi nạp).
     */
    suspend fun applyCashTopup(
        expectedCardId: String,
        serverBalanceBefore: Int,
        doServerTopup: suspend () -> Result<Int>,
        adminPin: String = NfcConstants.DEFAULT_ADMIN_PIN
    ): Result<Int> = runOnNfc(adminPin) {
        val detectedCardId = manager.readCustomerInfo()["cardUUID"]?.trim().orEmpty()
        if (detectedCardId.isBlank()) error("Không đọc được cardId trên thẻ.")
        if (!detectedCardId.equals(expectedCardId, ignoreCase = true)) {
            error("Thẻ đang đặt không đúng với thẻ vừa quẹt.")
        }

        if (!manager.setBalance(serverBalanceBefore)) {
            error("Không đồng bộ được số dư server xuống thẻ.")
        }

        val newBalance = doServerTopup()
            .getOrElse { e -> error(e.message ?: "Nạp tiền server thất bại.") }

        if (!manager.setBalance(newBalance)) {
            error("Nạp tiền server thành công nhưng cập nhật thẻ thất bại.")
        }

        newBalance
    }

    /**
     * Đồng bộ số dư mới từ server xuống thẻ (sau khi MoMo / nguồn khác đã xác nhận thanh toán).
     * Validate cardId trước để chắc thẻ đang đặt đúng với thẻ vừa quẹt.
     */
    suspend fun syncBalanceToCard(
        expectedCardId: String,
        newBalance: Int,
        adminPin: String = NfcConstants.DEFAULT_ADMIN_PIN
    ): Result<Unit> = runOnNfc(adminPin) {
        val detectedCardId = manager.readCustomerInfo()["cardUUID"]?.trim().orEmpty()
        if (detectedCardId.isBlank()) error("Không đọc được cardId trên thẻ.")
        if (!detectedCardId.equals(expectedCardId, ignoreCase = true)) {
            error("Thẻ đang đặt không đúng với thẻ vừa quẹt.")
        }
        if (!manager.setBalance(newBalance)) error("Cập nhật số dư lên thẻ thất bại.")
    }

    /**
     * Common wrapper: chạy block trên IO, đảm bảo connect/verify trước và disconnect sau (kể cả khi lỗi).
     */
    private suspend inline fun <T> runOnNfc(
        adminPin: String,
        crossinline block: suspend () -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            manager.connectAndVerifyAdminPINEncrypted(adminPin = adminPin).getOrElse { e ->
                return@withContext Result.failure<T>(
                    Exception(e.message ?: "Không kết nối/xác thực được thẻ.")
                )
            }
            try {
                Result.success(block())
            } finally {
                manager.disconnect()
            }
        } catch (e: Exception) {
            // Đảm bảo disconnect ngay cả khi connect đã ok nhưng block throw.
            runCatching { manager.disconnect() }
            Result.failure(e)
        }
    }
}
