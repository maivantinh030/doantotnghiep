package com.park.services

import com.park.dto.*
import com.park.entities.BalanceTransaction
import com.park.entities.PaymentRecord
import com.park.repositories.*
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.math.BigDecimal
import io.ktor.client.engine.cio.*
import java.time.Instant
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class WalletService(
    private val userRepository: IUserRepository = UserRepository(),
    private val balanceTransactionRepository: IBalanceTransactionRepository = BalanceTransactionRepository(),
    private val paymentRepository: IPaymentRepository = PaymentRepository(),
    private val notificationService: NotificationService = NotificationService()
) {

    fun getBalance(userId: String): WalletBalanceDTO? {
        val user = userRepository.findById(userId) ?: return null
        return WalletBalanceDTO(currentBalance = user.currentBalance.toString())
    }

    fun getTransactions(userId: String, page: Int, size: Int, type: String?): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()
        val transactions = if (type != null) {
            balanceTransactionRepository.findByUserIdAndType(userId, type, size, offset)
        } else {
            balanceTransactionRepository.findByUserId(userId, size, offset)
        }
        val total = balanceTransactionRepository.countByUserId(userId)
        val totalPages = if (size > 0) ((total + size - 1) / size) else 1
        return mapOf(
            "items" to transactions.map { BalanceTransactionDTO.fromEntity(it) },
            "total" to total,
            "page" to page,
            "size" to size,
            "totalPages" to totalPages
        )
    }

    fun topUp(userId: String, request: TopUpRequest): Result<PaymentRecordDTO> {
        val amount: BigDecimal
        try {
            amount = BigDecimal(request.amount)
            if (amount <= BigDecimal.ZERO) {
                return Result.failure(IllegalArgumentException("Số tiền nạp phải lớn hơn 0"))
            }
        } catch (e: NumberFormatException) {
            return Result.failure(IllegalArgumentException("Số tiền không hợp lệ"))
        }

        val validMethods = listOf("MOMO", "VNPAY", "BANKING", "CASH")
        if (request.method !in validMethods) {
            return Result.failure(IllegalArgumentException("Phương thức thanh toán không hợp lệ"))
        }

        val user = userRepository.findById(userId)
            ?: return Result.failure(NoSuchElementException("User không tồn tại"))

        val now = Instant.now()
        val paymentId = UUID.randomUUID().toString()

        val payment = PaymentRecord(
            paymentId = paymentId,
            userId = userId,
            method = request.method,
            amount = amount,
            status = "SUCCESS",
            createdAt = now,
            orderId = null,
            momoTransId = null,
            qrData = null,
            completedAt = null

        )
        paymentRepository.create(payment)

        val newBalance = user.currentBalance.add(amount)
        userRepository.update(userId, mapOf("currentBalance" to newBalance))
        balanceTransactionRepository.create(
            BalanceTransaction(
                transactionId = UUID.randomUUID().toString(),
                userId = userId,
                amount = amount,
                balanceBefore = user.currentBalance,
                balanceAfter = newBalance,
                type = "TOPUP",
                referenceType = "PAYMENT",
                referenceId = paymentId,
                description = "Nạp tiền qua ${request.method}",
                createdAt = now,
                createdBy = null
            )
        )

        notificationService.createNotification(
            userId = userId,
            type = "TOPUP",
            title = "Nạp tiền thành công",
            message = "Bạn vừa nạp ${amount.stripTrailingZeros().toPlainString()} VND qua ${request.method}. Số dư hiện tại: ${newBalance.stripTrailingZeros().toPlainString()} VND.",
            data = null
        )

        return Result.success(
            PaymentRecordDTO(
                paymentId = payment.paymentId,
                userId = payment.userId,
                method = payment.method,
                amount = payment.amount.toString(),
                status = payment.status,
                currentBalanceAfter = newBalance.toString(),
                createdAt = payment.createdAt.toString()
            )
        )
    }
    // Bước 1: Tạo QR MoMo — trả về qrCodeUrl để client hiển thị
    suspend fun createMomoTopUp(userId: String, request: TopUpRequest): Result<PaymentRecordDTO> {
        val amount: BigDecimal
        try {
            amount = BigDecimal(request.amount)
            if (amount <= BigDecimal.ZERO)
                return Result.failure(IllegalArgumentException("Số tiền nạp phải lớn hơn 0"))
        } catch (e: NumberFormatException) {
            return Result.failure(IllegalArgumentException("Số tiền không hợp lệ"))
        }

        userRepository.findById(userId)
            ?: return Result.failure(NoSuchElementException("User không tồn tại"))

        val paymentId = UUID.randomUUID().toString()
        val orderId   = "QR_${System.currentTimeMillis()}"
        val requestId = "REQ_${System.currentTimeMillis()}"

        // Config MoMo
        val partnerCode = "MOMO"
        val accessKey   = "F8BBA842ECF85"
        val secretKey   = "K951B6PE1waDMi640xX08PD3vg6EkVlz"
        val redirectUrl = "momoapp://callback"
        val ipnUrl      = "https://aged-flirt-activity.ngrok-free.dev/api/wallet/momo/ipn"  // đổi lại
        val requestType = "captureWallet"
        val extraData   = ""
        val orderInfo   = request.description?.trim()?.takeIf { it.isNotEmpty() } ?: "Nạp tiền ví Park"

        val rawSignature = "accessKey=$accessKey" +
                "&amount=${amount.toLong()}" +
                "&extraData=$extraData" +
                "&ipnUrl=$ipnUrl" +
                "&orderId=$orderId" +
                "&orderInfo=$orderInfo" +
                "&partnerCode=$partnerCode" +
                "&redirectUrl=$redirectUrl" +
                "&requestId=$requestId" +
                "&requestType=$requestType"

        val signature = hmacSHA256(rawSignature, secretKey)

        val requestBody = buildJsonObject {
            put("partnerCode", partnerCode)
            put("accessKey", accessKey)
            put("requestId", requestId)
            put("amount", amount.toLong())
            put("orderId", orderId)
            put("orderInfo", orderInfo)
            put("redirectUrl", redirectUrl)
            put("ipnUrl", ipnUrl)
            put("requestType", requestType)
            put("extraData", extraData)
            put("lang", "vi")
            put("signature", signature)
        }

        val client = HttpClient(CIO)
        try {
            val momoRes = client.post("https://test-payment.momo.vn/v2/gateway/api/create") {
                contentType(ContentType.Application.Json)
                setBody(requestBody.toString())
            }

            val json      = Json.parseToJsonElement(momoRes.bodyAsText()).jsonObject
            val resultCode = json["resultCode"]?.jsonPrimitive?.intOrNull
            val payUrl    = json["payUrl"]?.jsonPrimitive?.contentOrNull
            val momoQrData = json["qrCodeUrl"]?.jsonPrimitive?.contentOrNull
                ?: json["momoQrCodeUrl"]?.jsonPrimitive?.contentOrNull

            if (resultCode != 0 || payUrl == null) {
                return Result.failure(IllegalStateException(
                    json["message"]?.jsonPrimitive?.contentOrNull ?: "Lỗi tạo QR MoMo"
                ))
            }

            // Lưu PaymentRecord với status PENDING
            val payment = PaymentRecord(
                paymentId   = paymentId,
                userId      = userId,
                method      = "MOMO",
                amount      = amount,
                status      = "PENDING",
                createdAt   = Instant.now(),
                orderId     = orderId,
                qrData      = payUrl,
                momoTransId = null,
                completedAt = Instant.now()

            )
            paymentRepository.create(payment)

            val qrPayload = momoQrData?.takeIf { it.isNotBlank() } ?: payUrl
            val qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=400x400" +
                    "&data=${java.net.URLEncoder.encode(qrPayload, "UTF-8")}"

            return Result.success(
                PaymentRecordDTO(
                    paymentId            = paymentId,
                    userId               = userId,
                    method               = "MOMO",
                    amount               = amount.toString(),
                    status               = "PENDING",
                    currentBalanceAfter  = null,  // chưa cộng tiền
                    createdAt            = payment.createdAt.toString(),
                    orderId              = orderId,
                    payUrl               = payUrl,
                    qrCodeUrl            = qrCodeUrl
                )
            )
        } finally {
            client.close()
        }
    }

    // Bước 2: MoMo gọi IPN → cộng tiền vào ví
    fun handleMomoIpn(ipnBody: String): Boolean {
        val json       = Json.parseToJsonElement(ipnBody).jsonObject
        val orderId    = json["orderId"]?.jsonPrimitive?.contentOrNull ?: return false
        val resultCode = json["resultCode"]?.jsonPrimitive?.intOrNull ?: return false
        val momoTransId = json["transId"]?.jsonPrimitive?.contentOrNull

        val payment = paymentRepository.findByOrderId(orderId) ?: return false

        // Giao dịch không phải PENDING → IPN trùng, bỏ qua
        if (payment.status != "PENDING") return true

        if (resultCode != 0) {
            paymentRepository.updateStatus(payment.paymentId, "FAILED")
            return true
        }

        val user = userRepository.findById(payment.userId) ?: return false
        val now  = Instant.now()
        val newBalance = user.currentBalance.add(payment.amount)

        // Cộng tiền
        userRepository.update(payment.userId, mapOf("currentBalance" to newBalance))

        // Cập nhật payment → SUCCESS
        paymentRepository.updateMomoSuccess(orderId, momoTransId ?: "")

        // Ghi lịch sử
        balanceTransactionRepository.create(
            BalanceTransaction(
                transactionId = UUID.randomUUID().toString(),
                userId        = payment.userId,
                amount        = payment.amount,
                balanceBefore = user.currentBalance,
                balanceAfter  = newBalance,
                type          = "TOPUP",
                referenceType = "PAYMENT",
                referenceId   = payment.paymentId,
                description   = "Nạp tiền qua MoMo",
                createdAt     = now,
                createdBy     = null
            )
        )

        notificationService.createNotification(
            userId = payment.userId,
            type = "TOPUP",
            title = "Nạp tiền MoMo thành công",
            message = "Bạn vừa nạp ${payment.amount.stripTrailingZeros().toPlainString()} VND qua MoMo. Số dư hiện tại: ${newBalance.stripTrailingZeros().toPlainString()} VND.",
            data = null
        )

        return true
    }
    fun getPaymentByOrderId(userId: String, orderId: String): PaymentRecordDTO? {
        val payment = paymentRepository.findByOrderId(orderId) ?: return null
        // Đảm bảo user chỉ xem được giao dịch của mình
        if (payment.userId != userId) return null
        return PaymentRecordDTO.fromEntity(payment)
    }
    // Trừ tiền khi chơi game — gọi bởi GameService sau khi verify RSA
    fun deductForGamePlay(
        userId: String,
        amount: BigDecimal,
        gameId: String,
        cardId: String?,
        staffId: String? = null
    ): Result<BigDecimal> {
        val user = userRepository.findById(userId)
            ?: return Result.failure(NoSuchElementException("User không tồn tại"))

        if (user.currentBalance < amount) {
            return Result.failure(IllegalStateException("Số dư không đủ"))
        }

        val now = Instant.now()
        val newBalance = user.currentBalance.subtract(amount)
        userRepository.update(userId, mapOf("currentBalance" to newBalance))

        balanceTransactionRepository.create(
            BalanceTransaction(
                transactionId = UUID.randomUUID().toString(),
                userId = userId,
                amount = amount.negate(),
                balanceBefore = user.currentBalance,
                balanceAfter = newBalance,
                type = "PAYMENT",
                referenceType = "GAME",
                referenceId = gameId,
                description = "Chơi game",
                createdAt = now,
                createdBy = staffId
            )
        )

        return Result.success(newBalance)
    }

    fun getPaymentHistory(userId: String, page: Int, size: Int): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()
        val payments = paymentRepository.findByUserId(userId, size, offset)
        val total = paymentRepository.countByUserId(userId)
        val totalPages = if (size > 0) ((total + size - 1) / size) else 1
        return mapOf(
            "items" to payments.map { PaymentRecordDTO.fromEntity(it) },
            "total" to total,
            "page" to page,
            "size" to size,
            "totalPages" to totalPages
        )
    }
    fun hmacSHA256(data: String, key: String): String {
        val algorithm = "HmacSHA256"
        val mac = Mac.getInstance(algorithm)
        val secretKeySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), algorithm)
        mac.init(secretKeySpec)
        return mac.doFinal(data.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
