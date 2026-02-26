package com.park.services

import com.park.dto.*
import com.park.entities.BalanceTransaction
import com.park.entities.PaymentRecord
import com.park.repositories.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class WalletService(
    private val userRepository: IUserRepository = UserRepository(),
    private val balanceTransactionRepository: IBalanceTransactionRepository = BalanceTransactionRepository(),
    private val paymentRepository: IPaymentRepository = PaymentRepository()
) {

    fun getBalance(userId: String): WalletBalanceDTO? {
        val user = userRepository.findById(userId) ?: return null
        return WalletBalanceDTO(
            currentBalance = user.currentBalance.toString(),
            loyaltyPoints = user.loyaltyPoints
        )
    }

    fun getTransactions(userId: String, page: Int, size: Int, type: String?): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()
        val transactions = if (type != null) {
            balanceTransactionRepository.findByUserIdAndType(userId, type, size, offset)
        } else {
            balanceTransactionRepository.findByUserId(userId, size, offset)
        }
        val total = balanceTransactionRepository.countByUserId(userId)

        return mapOf(
            "items" to transactions.map { BalanceTransactionDTO.fromEntity(it) },
            "total" to total,
            "page" to page,
            "size" to size
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

        if (request.method !in listOf("MOMO", "VNPAY", "BANKING", "CASH")) {
            return Result.failure(IllegalArgumentException("Phương thức thanh toán không hợp lệ"))
        }

        val user = userRepository.findById(userId)
            ?: return Result.failure(NoSuchElementException("User không tồn tại"))

        val paymentId = UUID.randomUUID().toString()
        val now = Instant.now()

        // Tạo payment record
        val payment = PaymentRecord(
            paymentId = paymentId,
            userId = userId,
            method = request.method,
            amount = amount,
            status = "SUCCESS", // Tạm auto-success, thực tế sẽ cần callback từ payment gateway
            createdAt = now
        )
        paymentRepository.create(payment)

        // Cộng tiền vào ví
        val newBalance = user.currentBalance.add(amount)
        userRepository.update(userId, mapOf("currentBalance" to newBalance))

        // Ghi log giao dịch
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

        return Result.success(PaymentRecordDTO.fromEntity(payment))
    }

    fun getPaymentHistory(userId: String, page: Int, size: Int): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()
        val payments = paymentRepository.findByUserId(userId, size, offset)
        val total = paymentRepository.countByUserId(userId)

        return mapOf(
            "payments" to payments.map { PaymentRecordDTO.fromEntity(it) },
            "total" to total,
            "page" to page,
            "size" to size
        )
    }
}
