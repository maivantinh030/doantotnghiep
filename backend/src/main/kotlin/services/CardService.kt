package com.park.services

import com.park.dto.CardDTO
import com.park.dto.IssueCardRequest
import com.park.dto.RegisterCardRequest
import com.park.entities.BalanceTransaction
import com.park.entities.Card
import com.park.repositories.BalanceTransactionRepository
import com.park.repositories.CardRepository
import com.park.repositories.IBalanceTransactionRepository
import com.park.repositories.ICardRepository
import com.park.repositories.IUserRepository
import com.park.repositories.UserRepository
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class CardService(
    private val cardRepository: ICardRepository = CardRepository(),
    private val userRepository: IUserRepository = UserRepository(),
    private val balanceTransactionRepository: IBalanceTransactionRepository = BalanceTransactionRepository()
) {

    fun getMyCards(userId: String): List<CardDTO> {
        return cardRepository.findByUserId(userId).map { CardDTO.fromEntity(it) }
    }

    fun getCardById(cardId: String, userId: String): CardDTO? {
        val card = cardRepository.findById(cardId) ?: return null
        if (card.userId != userId) return null
        return CardDTO.fromEntity(card)
    }

    fun registerCard(request: RegisterCardRequest): Result<CardDTO> {
        val normalizedCardId = request.cardId.trim()
        if (normalizedCardId.isBlank()) {
            return Result.failure(IllegalArgumentException("cardId khong hop le"))
        }
        if (
            cardRepository.findById(normalizedCardId) != null ||
            cardRepository.findByPhysicalUid(normalizedCardId) != null
        ) {
            return Result.failure(IllegalStateException("Ma the da ton tai trong he thong"))
        }

        val now = Instant.now()
        val card = Card(
            cardId = normalizedCardId,
            userId = null,
            cardName = request.cardName,
            status = "AVAILABLE",
            depositAmount = BigDecimal.ZERO,
            depositStatus = "NONE",
            issuedAt = null,
            blockedAt = null,
            blockedReason = null,
            lastUsedAt = null,
            createdAt = now,
            updatedAt = now
        )
        val created = cardRepository.create(card)
        return Result.success(CardDTO.fromEntity(created))
    }

    fun issueCard(request: IssueCardRequest, staffId: String): Result<CardDTO> {
        val card = cardRepository.findById(request.cardId)
            ?: return Result.failure(NoSuchElementException("The khong ton tai"))
        if (card.status != "AVAILABLE") {
            return Result.failure(IllegalStateException("The khong o trang thai co the phat hanh"))
        }
        val user = userRepository.findById(request.userId)
            ?: return Result.failure(NoSuchElementException("Tai khoan nguoi dung khong ton tai"))

        val depositAmount: BigDecimal = try {
            BigDecimal(request.depositAmount).also {
                if (it < BigDecimal.ZERO) {
                    return Result.failure(IllegalArgumentException("Tien coc khong hop le"))
                }
            }
        } catch (_: NumberFormatException) {
            return Result.failure(IllegalArgumentException("So tien coc khong hop le"))
        }

        val now = Instant.now()

        cardRepository.update(
            request.cardId,
            mapOf(
                "userId" to request.userId,
                "cardName" to request.cardName,
                "status" to "ACTIVE",
                "depositAmount" to depositAmount,
                "depositStatus" to "PAID",
                "issuedAt" to now
            )
        )

        balanceTransactionRepository.create(
            BalanceTransaction(
                transactionId = UUID.randomUUID().toString(),
                userId = request.userId,
                amount = depositAmount.negate(),
                balanceBefore = user.currentBalance,
                balanceAfter = user.currentBalance,
                type = "DEPOSIT_PAID",
                referenceType = "CARD",
                referenceId = request.cardId,
                description = "Thu tien coc the ${request.cardId}",
                createdAt = now,
                createdBy = staffId
            )
        )

        val updated = cardRepository.findById(request.cardId)!!
        return Result.success(CardDTO.fromEntity(updated))
    }

    fun returnCard(cardId: String, staffId: String): Result<Map<String, Any>> {
        val card = cardRepository.findById(cardId)
            ?: return Result.failure(NoSuchElementException("The khong ton tai"))
        if (card.status == "AVAILABLE") {
            return Result.failure(IllegalStateException("The chua duoc lien ket voi tai khoan nao"))
        }

        val userId = card.userId
            ?: return Result.failure(IllegalStateException("The khong co tai khoan lien ket"))
        val user = userRepository.findById(userId)
            ?: return Result.failure(NoSuchElementException("Tai khoan khong ton tai"))

        val now = Instant.now()
        val refundDeposit = if (card.depositStatus == "PAID") card.depositAmount else BigDecimal.ZERO
        val refundBalance = user.currentBalance

        if (refundBalance > BigDecimal.ZERO) {
            val newBalance = BigDecimal.ZERO
            userRepository.update(userId, mapOf("currentBalance" to newBalance))
            balanceTransactionRepository.create(
                BalanceTransaction(
                    transactionId = UUID.randomUUID().toString(),
                    userId = userId,
                    amount = refundBalance.negate(),
                    balanceBefore = refundBalance,
                    balanceAfter = newBalance,
                    type = "REFUND",
                    referenceType = "CARD",
                    referenceId = cardId,
                    description = "Hoan so du khi tra the",
                    createdAt = now,
                    createdBy = staffId
                )
            )
        }

        if (refundDeposit > BigDecimal.ZERO) {
            val currentBalance = userRepository.findById(userId)!!.currentBalance
            balanceTransactionRepository.create(
                BalanceTransaction(
                    transactionId = UUID.randomUUID().toString(),
                    userId = userId,
                    amount = refundDeposit,
                    balanceBefore = currentBalance,
                    balanceAfter = currentBalance,
                    type = "DEPOSIT_REFUND",
                    referenceType = "CARD",
                    referenceId = cardId,
                    description = "Hoan tien coc khi tra the",
                    createdAt = now,
                    createdBy = staffId
                )
            )
        }

        cardRepository.update(
            cardId,
            mapOf(
                "userId" to null,
                "status" to "AVAILABLE",
                "depositAmount" to BigDecimal.ZERO,
                "depositStatus" to "NONE",
                "issuedAt" to null
            )
        )

        return Result.success(
            mapOf(
                "cardId" to cardId,
                "refundedBalance" to refundBalance.toString(),
                "refundedDeposit" to refundDeposit.toString()
            )
        )
    }

    fun blockCard(cardId: String, reason: String?, staffId: String): Result<CardDTO> {
        val card = cardRepository.findById(cardId)
            ?: return Result.failure(NoSuchElementException("The khong ton tai"))
        if (card.status == "BLOCKED") {
            return Result.failure(IllegalStateException("The da bi khoa"))
        }

        val userId = card.userId
        val now = Instant.now()

        if (userId != null && card.depositStatus == "PAID") {
            val user = userRepository.findById(userId)
            if (user != null) {
                balanceTransactionRepository.create(
                    BalanceTransaction(
                        transactionId = UUID.randomUUID().toString(),
                        userId = userId,
                        amount = card.depositAmount.negate(),
                        balanceBefore = user.currentBalance,
                        balanceAfter = user.currentBalance,
                        type = "DEPOSIT_FORFEITED",
                        referenceType = "CARD",
                        referenceId = cardId,
                        description = "Mat tien coc do mat the",
                        createdAt = now,
                        createdBy = staffId
                    )
                )
            }
        }

        cardRepository.update(
            cardId,
            mapOf(
                "status" to "BLOCKED",
                "depositStatus" to if (card.depositStatus == "PAID") "FORFEITED" else card.depositStatus,
                "blockedAt" to now,
                "blockedReason" to reason
            )
        )
        val updated = cardRepository.findById(cardId)!!
        return Result.success(CardDTO.fromEntity(updated))
    }

    fun processCardTap(cardId: String): Result<CardDTO> {
        val normalizedCardId = cardId.trim()
        if (normalizedCardId.isBlank()) {
            return Result.failure(IllegalArgumentException("cardId khong hop le"))
        }

        val card = cardRepository.findById(normalizedCardId)
            ?: cardRepository.findByPhysicalUid(normalizedCardId)
            ?: return Result.failure(NoSuchElementException("The khong ton tai trong he thong"))
        if (card.status == "BLOCKED") {
            return Result.failure(IllegalStateException("The da bi khoa"))
        }
        if (card.status == "AVAILABLE" || card.userId == null) {
            return Result.failure(IllegalStateException("The chua duoc lien ket voi tai khoan"))
        }

        cardRepository.update(card.cardId, mapOf("lastUsedAt" to Instant.now()))
        val updated = cardRepository.findById(card.cardId)!!
        return Result.success(CardDTO.fromEntity(updated))
    }

    fun getAvailableCards(): List<CardDTO> {
        return cardRepository.findAvailable().map { CardDTO.fromEntity(it) }
    }
}
