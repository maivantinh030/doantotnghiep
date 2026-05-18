package com.park.services

import com.park.dto.ApproveCardRequestDTO
import com.park.dto.CardRequestDTO
import com.park.dto.CardRequestUserInfoDTO
import com.park.dto.CreateCardRequestDTO
import com.park.dto.IssueCardFromRequestDTO
import com.park.entities.BalanceTransaction
import com.park.entities.Card
import com.park.entities.CardRequest
import com.park.repositories.AccountRepository
import com.park.repositories.BalanceTransactionRepository
import com.park.repositories.CardRepository
import com.park.repositories.CardRequestRepository
import com.park.repositories.IAccountRepository
import com.park.repositories.IBalanceTransactionRepository
import com.park.repositories.ICardRepository
import com.park.repositories.ICardRequestRepository
import com.park.repositories.IUserRepository
import com.park.repositories.UserRepository
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class CardRequestService(
    private val cardRequestRepository: ICardRequestRepository = CardRequestRepository(),
    private val userRepository: IUserRepository = UserRepository(),
    private val accountRepository: IAccountRepository = AccountRepository(),
    private val cardRepository: ICardRepository = CardRepository(),
    private val balanceTransactionRepository: IBalanceTransactionRepository = BalanceTransactionRepository(),
    private val rsaService: RSAService = RSAService(),
    private val notificationService: NotificationService = NotificationService()
) {

    fun createRequest(userId: String, dto: CreateCardRequestDTO): Result<CardRequestDTO> {
        val depositAmount: BigDecimal = try {
            BigDecimal(dto.depositAmount).also {
                if (it < BigDecimal.ZERO) {
                    return Result.failure(IllegalArgumentException("Số tiền cọc không hợp lệ"))
                }
            }
        } catch (_: NumberFormatException) {
            return Result.failure(IllegalArgumentException("Số tiền cọc không hợp lệ"))
        }

        if (cardRepository.findActiveByUserId(userId) != null) {
            return Result.failure(IllegalStateException("Bạn đang có sẵn thẻ, không thể tạo thêm"))
        }

        val existing = cardRequestRepository.findByUserId(userId)
            .firstOrNull { it.status == "PENDING" }
        if (existing != null) {
            return Result.failure(IllegalStateException("Bạn đã có yêu cầu cấp thẻ đang chờ duyệt"))
        }

        val user = userRepository.findById(userId)
            ?: return Result.failure(NoSuchElementException("Người dùng không tồn tại"))

        if (dto.depositPaidOnline && depositAmount > BigDecimal.ZERO && user.currentBalance < depositAmount) {
            return Result.failure(IllegalStateException("Số dư không đủ để cọc"))
        }

        val now = Instant.now()
        val req = CardRequest(
            requestId = UUID.randomUUID().toString(),
            userId = userId,
            status = "PENDING",
            depositPaidOnline = dto.depositPaidOnline,
            depositAmount = depositAmount,
            note = dto.note,
            approvedBy = null,
            createdAt = now,
            updatedAt = now
        )
        val created = cardRequestRepository.create(req)

        if (dto.depositPaidOnline && depositAmount > BigDecimal.ZERO) {
            val newBalance = user.currentBalance.subtract(depositAmount)
            userRepository.update(userId, mapOf("currentBalance" to newBalance))
            balanceTransactionRepository.create(
                BalanceTransaction(
                    transactionId = UUID.randomUUID().toString(),
                    userId = userId,
                    amount = depositAmount.negate(),
                    balanceBefore = user.currentBalance,
                    balanceAfter = newBalance,
                    type = "DEPOSIT_PAID",
                    referenceType = "CARD_REQUEST",
                    referenceId = created.requestId,
                    description = "Tiền cọc thẻ",
                    createdAt = now,
                    createdBy = null
                )
            )
            notificationService.createNotification(
                userId = userId,
                type = "DEPOSIT_PAID",
                title = "Đã đặt cọc thẻ",
                message = "Bạn vừa cọc ${depositAmount.stripTrailingZeros().toPlainString()} VND cho yêu cầu cấp thẻ. Số dư hiện tại: ${newBalance.stripTrailingZeros().toPlainString()} VND."
            )
        }

        return Result.success(buildCardRequestDTO(created))
    }

    fun getMyRequests(userId: String): List<CardRequestDTO> {
        return cardRequestRepository.findByUserId(userId).map(::buildCardRequestDTO)
    }

    fun getRequestsByStatus(status: String): List<CardRequestDTO> {
        val requests = if (status == "COMPLETED") {
            (cardRequestRepository.findByStatus("COMPLETED") + cardRequestRepository.findByStatus("APPROVED"))
                .distinctBy { it.requestId }
                .sortedByDescending { it.createdAt }
        } else {
            cardRequestRepository.findByStatus(status)
        }
        return requests.map(::buildCardRequestDTO)
    }

    fun reviewRequest(requestId: String, dto: ApproveCardRequestDTO, adminId: String): Result<CardRequestDTO> {
        val req = cardRequestRepository.findById(requestId)
            ?: return Result.failure(NoSuchElementException("Yêu cầu không tồn tại"))
        if (req.status != "PENDING") {
            return Result.failure(IllegalStateException("Yêu cầu này đã được xử lý"))
        }

        val newStatus = if (dto.approved) "COMPLETED" else "REJECTED"
        cardRequestRepository.update(
            requestId,
            mapOf(
                "status" to newStatus,
                "approvedBy" to adminId,
                "note" to (dto.note ?: req.note)
            )
        )

        if (!dto.approved && req.depositPaidOnline && req.depositAmount > BigDecimal.ZERO) {
            val user = userRepository.findById(req.userId)
            if (user != null) {
                val now = Instant.now()
                val newBalance = user.currentBalance.add(req.depositAmount)
                userRepository.update(req.userId, mapOf("currentBalance" to newBalance))
                balanceTransactionRepository.create(
                    BalanceTransaction(
                        transactionId = UUID.randomUUID().toString(),
                        userId = req.userId,
                        amount = req.depositAmount,
                        balanceBefore = user.currentBalance,
                        balanceAfter = newBalance,
                        type = "DEPOSIT_REFUND",
                        referenceType = "CARD_REQUEST",
                        referenceId = req.requestId,
                        description = "Hoàn tiền cọc do yêu cầu bị từ chối",
                        createdAt = now,
                        createdBy = adminId
                    )
                )
                notificationService.createNotification(
                    userId = req.userId,
                    type = "DEPOSIT_REFUND",
                    title = "Đã hoàn tiền cọc",
                    message = "Yêu cầu cấp thẻ của bạn bị từ chối. Đã hoàn ${req.depositAmount.stripTrailingZeros().toPlainString()} VND tiền cọc. Số dư hiện tại: ${newBalance.stripTrailingZeros().toPlainString()} VND."
                )
            }
        }

        val updated = cardRequestRepository.findById(requestId)!!
        return Result.success(buildCardRequestDTO(updated))
    }

    fun cancelRequest(requestId: String, userId: String): Result<CardRequestDTO> {
        val req = cardRequestRepository.findById(requestId)
            ?: return Result.failure(NoSuchElementException("Yêu cầu không tồn tại"))

        if (req.userId != userId) {
            return Result.failure(IllegalStateException("Không có quyền hủy yêu cầu này"))
        }
        if (req.status != "PENDING") {
            return Result.failure(IllegalStateException("Chỉ có thể hủy yêu cầu đang chờ duyệt"))
        }

        cardRequestRepository.update(requestId, mapOf("status" to "CANCELED"))

        if (req.depositPaidOnline && req.depositAmount > BigDecimal.ZERO) {
            val user = userRepository.findById(req.userId)
            if (user != null) {
                val now = Instant.now()
                val newBalance = user.currentBalance.add(req.depositAmount)
                userRepository.update(req.userId, mapOf("currentBalance" to newBalance))
                balanceTransactionRepository.create(
                    BalanceTransaction(
                        transactionId = UUID.randomUUID().toString(),
                        userId = req.userId,
                        amount = req.depositAmount,
                        balanceBefore = user.currentBalance,
                        balanceAfter = newBalance,
                        type = "DEPOSIT_REFUND",
                        referenceType = "CARD_REQUEST",
                        referenceId = req.requestId,
                        description = "Hoàn tiền cọc do người dùng hủy yêu cầu",
                        createdAt = now,
                        createdBy = null
                    )
                )
                notificationService.createNotification(
                    userId = req.userId,
                    type = "DEPOSIT_REFUND",
                    title = "Đã hoàn tiền cọc",
                    message = "Bạn vừa hủy yêu cầu cấp thẻ. Đã hoàn ${req.depositAmount.stripTrailingZeros().toPlainString()} VND tiền cọc. Số dư hiện tại: ${newBalance.stripTrailingZeros().toPlainString()} VND."
                )
            }
        }

        val updated = cardRequestRepository.findById(requestId)!!
        return Result.success(buildCardRequestDTO(updated))
    }

    fun completeRequest(requestId: String, adminId: String): Result<CardRequestDTO> {
        val req = cardRequestRepository.findById(requestId)
            ?: return Result.failure(NoSuchElementException("Yêu cầu không tồn tại"))
        if (req.status !in listOf("PENDING", "APPROVED")) {
            return Result.failure(IllegalStateException("Yêu cầu này không thể hoàn thành"))
        }

        cardRequestRepository.update(
            requestId,
            mapOf(
                "status" to "COMPLETED",
                "approvedBy" to adminId
            )
        )
        val updated = cardRequestRepository.findById(requestId)!!
        return Result.success(buildCardRequestDTO(updated))
    }

    fun issueCardForRequest(
        requestId: String,
        dto: IssueCardFromRequestDTO,
        adminId: String
    ): Result<CardRequestDTO> {
        val req = cardRequestRepository.findById(requestId)
            ?: return Result.failure(NoSuchElementException("Yêu cầu không tồn tại"))

        if (req.status !in listOf("PENDING", "APPROVED")) {
            return Result.failure(IllegalStateException("Yêu cầu này không thể cấp thẻ"))
        }

        val user = userRepository.findById(req.userId)
            ?: return Result.failure(NoSuchElementException("Người dùng không tồn tại"))
        if (cardRepository.findActiveByUserId(req.userId) != null) {
            return Result.failure(
                IllegalStateException("Người dùng đang có thẻ đang hoạt động, không thể cấp thêm thẻ mới")
            )
        }

        val normalizedCardId = dto.cardId.trim()
        if (normalizedCardId.isBlank()) {
            return Result.failure(IllegalArgumentException("Mã thẻ không hợp lệ"))
        }
        if (dto.publicKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Thiếu public key của thẻ"))
        }
        if (
            cardRepository.findById(normalizedCardId) != null ||
            cardRepository.findByPhysicalUid(normalizedCardId) != null
        ) {
            return Result.failure(IllegalStateException("Mã thẻ đã tồn tại trong hệ thống"))
        }

        val now = Instant.now()
        val depositStatus = if (req.depositAmount > BigDecimal.ZERO) "PAID" else "NONE"

        val createdCard = cardRepository.create(
            Card(
                cardId = normalizedCardId,
                userId = req.userId,
                cardName = null,
                status = "ACTIVE",
                depositAmount = req.depositAmount,
                depositStatus = depositStatus,
                issuedAt = now,
                blockedAt = null,
                blockedReason = null,
                lastUsedAt = null,
                createdAt = now,
                updatedAt = now
            )
        )

        if (!req.depositPaidOnline && req.depositAmount > BigDecimal.ZERO) {
            balanceTransactionRepository.create(
                BalanceTransaction(
                    transactionId = UUID.randomUUID().toString(),
                    userId = req.userId,
                    amount = req.depositAmount.negate(),
                    balanceBefore = user.currentBalance,
                    balanceAfter = user.currentBalance,
                    type = "DEPOSIT_PAID",
                    referenceType = "CARD",
                    referenceId = createdCard.cardId,
                    description = "Thu tiền cọc thẻ ${createdCard.cardId}",
                    createdAt = now,
                    createdBy = adminId
                )
            )
        }

        rsaService.registerPublicKey(createdCard.cardId, dto.publicKey).getOrElse { e ->
            return Result.failure(IllegalStateException("Lưu public key thất bại: ${e.message}"))
        }

        cardRequestRepository.update(
            requestId,
            mapOf(
                "status" to "COMPLETED",
                "approvedBy" to adminId
            )
        )

        val updated = cardRequestRepository.findById(requestId)!!
        return Result.success(buildCardRequestDTO(updated))
    }

    private fun buildCardRequestDTO(req: CardRequest): CardRequestDTO {
        return CardRequestDTO.fromEntity(
            req = req,
            requester = loadRequesterInfo(req.userId),
            statusOverride = normalizeStatus(req.status)
        )
    }

    private fun normalizeStatus(status: String): String {
        return if (status == "APPROVED") "COMPLETED" else status
    }

    private fun loadRequesterInfo(userId: String): CardRequestUserInfoDTO? {
        val user = userRepository.findById(userId) ?: return null
        val phoneNumber = user.accountId
            ?.let(accountRepository::findById)
            ?.phoneNumber
            .orEmpty()

        return CardRequestUserInfoDTO.fromUser(user, phoneNumber)
    }
}
