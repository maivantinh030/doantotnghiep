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
            createdAt = now
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
}
