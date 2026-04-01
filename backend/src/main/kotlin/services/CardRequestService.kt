package com.park.services

import com.park.dto.ApproveCardRequestDTO
import com.park.dto.CardRequestDTO
import com.park.dto.CreateCardRequestDTO
import com.park.dto.IssueCardFromRequestDTO
import com.park.entities.BalanceTransaction
import com.park.entities.Card
import com.park.entities.CardRequest
import com.park.repositories.BalanceTransactionRepository
import com.park.repositories.CardRepository
import com.park.repositories.CardRequestRepository
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
    private val cardRepository: ICardRepository = CardRepository(),
    private val balanceTransactionRepository: IBalanceTransactionRepository = BalanceTransactionRepository(),
    private val rsaService: RSAService = RSAService()
) {

    fun createRequest(userId: String, dto: CreateCardRequestDTO): Result<CardRequestDTO> {
        val depositAmount: BigDecimal = try {
            BigDecimal(dto.depositAmount).also {
                if (it < BigDecimal.ZERO) {
                    return Result.failure(IllegalArgumentException("So tien coc khong hop le"))
                }
            }
        } catch (_: NumberFormatException) {
            return Result.failure(IllegalArgumentException("So tien coc khong hop le"))
        }

        val existing = cardRequestRepository.findByUserId(userId)
            .firstOrNull { it.status == "PENDING" }
        if (existing != null) {
            return Result.failure(IllegalStateException("Ban da co yeu cau cap the dang cho duyet"))
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
        return Result.success(CardRequestDTO.fromEntity(created))
    }

    fun getMyRequests(userId: String): List<CardRequestDTO> {
        return cardRequestRepository.findByUserId(userId).map { CardRequestDTO.fromEntity(it) }
    }

    fun getRequestsByStatus(status: String): List<CardRequestDTO> {
        return cardRequestRepository.findByStatus(status).map { CardRequestDTO.fromEntity(it) }
    }

    fun reviewRequest(requestId: String, dto: ApproveCardRequestDTO, adminId: String): Result<CardRequestDTO> {
        val req = cardRequestRepository.findById(requestId)
            ?: return Result.failure(NoSuchElementException("Yeu cau khong ton tai"))
        if (req.status != "PENDING") {
            return Result.failure(IllegalStateException("Yeu cau nay da duoc xu ly"))
        }

        val newStatus = if (dto.approved) "APPROVED" else "REJECTED"
        cardRequestRepository.update(
            requestId,
            mapOf(
                "status" to newStatus,
                "approvedBy" to adminId,
                "note" to (dto.note ?: req.note)
            )
        )
        val updated = cardRequestRepository.findById(requestId)!!
        return Result.success(CardRequestDTO.fromEntity(updated))
    }

    fun completeRequest(requestId: String, adminId: String): Result<CardRequestDTO> {
        val req = cardRequestRepository.findById(requestId)
            ?: return Result.failure(NoSuchElementException("Yeu cau khong ton tai"))
        if (req.status != "APPROVED") {
            return Result.failure(IllegalStateException("Yeu cau chua duoc duyet"))
        }

        cardRequestRepository.update(
            requestId,
            mapOf(
                "status" to "COMPLETED",
                "approvedBy" to adminId
            )
        )
        val updated = cardRequestRepository.findById(requestId)!!
        return Result.success(CardRequestDTO.fromEntity(updated))
    }

    fun issueCardForRequest(
        requestId: String,
        dto: IssueCardFromRequestDTO,
        adminId: String
    ): Result<CardRequestDTO> {
        val req = cardRequestRepository.findById(requestId)
            ?: return Result.failure(NoSuchElementException("Yeu cau khong ton tai"))

        if (req.status !in listOf("PENDING", "APPROVED")) {
            return Result.failure(IllegalStateException("Yeu cau nay khong the cap the"))
        }

        val user = userRepository.findById(req.userId)
            ?: return Result.failure(NoSuchElementException("Nguoi dung khong ton tai"))

        val normalizedCardId = dto.cardId.trim()
        if (normalizedCardId.isBlank()) {
            return Result.failure(IllegalArgumentException("cardId khong hop le"))
        }
        if (dto.publicKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Thieu public key cua the"))
        }
        if (
            cardRepository.findById(normalizedCardId) != null ||
            cardRepository.findByPhysicalUid(normalizedCardId) != null
        ) {
            return Result.failure(IllegalStateException("Ma the da ton tai trong he thong"))
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
                    description = "Thu tien coc the ${createdCard.cardId}",
                    createdAt = now,
                    createdBy = adminId
                )
            )
        }

        rsaService.registerPublicKey(createdCard.cardId, dto.publicKey).getOrElse { e ->
            return Result.failure(IllegalStateException("Luu public key that bai: ${e.message}"))
        }

        cardRequestRepository.update(
            requestId,
            mapOf(
                "status" to "COMPLETED",
                "approvedBy" to adminId
            )
        )

        val updated = cardRequestRepository.findById(requestId)!!
        return Result.success(CardRequestDTO.fromEntity(updated))
    }
}
