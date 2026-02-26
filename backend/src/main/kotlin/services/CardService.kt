package com.park.services

import com.park.dto.*
import com.park.entities.Card
import com.park.repositories.CardRepository
import com.park.repositories.ICardRepository
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.util.*

class CardService(
    private val cardRepository: ICardRepository = CardRepository()
) {

    fun getMyCards(userId: String): List<CardDTO> {
        return cardRepository.findByUserId(userId).map { CardDTO.fromEntity(it) }
    }

    fun getCardById(cardId: String, userId: String): CardDTO? {
        val card = cardRepository.findById(cardId) ?: return null
        if (card.userId != userId) return null
        return CardDTO.fromEntity(card)
    }

    fun linkCard(userId: String, request: LinkCardRequest): Result<CardDTO> {
        // Kiểm tra thẻ vật lý tồn tại
        val existingCard = cardRepository.findByPhysicalUid(request.physicalCardUid)

        if (existingCard != null) {
            // Thẻ đã được liên kết với user khác
            if (existingCard.userId != null && existingCard.userId != userId) {
                return Result.failure(IllegalStateException("Thẻ đã được liên kết với tài khoản khác"))
            }
            // Thẻ đã liên kết với chính user này
            if (existingCard.userId == userId) {
                return Result.failure(IllegalStateException("Thẻ đã được liên kết với tài khoản của bạn"))
            }
            // Thẻ trắng chưa liên kết -> liên kết
            val updates = mutableMapOf<String, Any?>(
                "userId" to userId,
                "status" to "ACTIVE",
                "issuedAt" to Instant.now()
            )
            request.cardName?.let { updates["cardName"] = it }
            request.pin?.let { updates["pinHash"] = BCrypt.hashpw(it, BCrypt.gensalt()) }

            cardRepository.update(existingCard.cardId, updates)
            val updated = cardRepository.findById(existingCard.cardId)!!
            return Result.success(CardDTO.fromEntity(updated))
        }

        // Tạo thẻ mới
        val now = Instant.now()
        val card = Card(
            cardId = UUID.randomUUID().toString(),
            physicalCardUid = request.physicalCardUid,
            userId = userId,
            cardName = request.cardName,
            status = "ACTIVE",
            pinHash = request.pin?.let { BCrypt.hashpw(it, BCrypt.gensalt()) },
            issuedAt = now,
            blockedAt = null,
            blockedReason = null,
            lastUsedAt = null,
            createdAt = now,
            updatedAt = now
        )
        val created = cardRepository.create(card)
        return Result.success(CardDTO.fromEntity(created))
    }

    fun updateCard(cardId: String, userId: String, request: UpdateCardRequest): Result<CardDTO> {
        val card = cardRepository.findById(cardId)
            ?: return Result.failure(NoSuchElementException("Thẻ không tồn tại"))
        if (card.userId != userId) {
            return Result.failure(IllegalAccessException("Không có quyền chỉnh sửa thẻ này"))
        }

        val updates = mutableMapOf<String, Any?>()
        request.cardName?.let { updates["cardName"] = it }
        request.pin?.let { updates["pinHash"] = BCrypt.hashpw(it, BCrypt.gensalt()) }

        if (updates.isNotEmpty()) {
            cardRepository.update(cardId, updates)
        }
        val updated = cardRepository.findById(cardId)!!
        return Result.success(CardDTO.fromEntity(updated))
    }

    fun blockCard(cardId: String, userId: String, reason: String?): Result<CardDTO> {
        val card = cardRepository.findById(cardId)
            ?: return Result.failure(NoSuchElementException("Thẻ không tồn tại"))
        if (card.userId != userId) {
            return Result.failure(IllegalAccessException("Không có quyền thao tác thẻ này"))
        }
        if (card.status == "BLOCKED") {
            return Result.failure(IllegalStateException("Thẻ đã bị khóa"))
        }

        cardRepository.update(cardId, mapOf(
            "status" to "BLOCKED",
            "blockedAt" to Instant.now(),
            "blockedReason" to reason
        ))
        val updated = cardRepository.findById(cardId)!!
        return Result.success(CardDTO.fromEntity(updated))
    }

    fun unblockCard(cardId: String, userId: String): Result<CardDTO> {
        val card = cardRepository.findById(cardId)
            ?: return Result.failure(NoSuchElementException("Thẻ không tồn tại"))
        if (card.userId != userId) {
            return Result.failure(IllegalAccessException("Không có quyền thao tác thẻ này"))
        }
        if (card.status != "BLOCKED") {
            return Result.failure(IllegalStateException("Thẻ không ở trạng thái bị khóa"))
        }

        cardRepository.update(cardId, mapOf(
            "status" to "ACTIVE",
            "blockedAt" to null,
            "blockedReason" to null
        ))
        val updated = cardRepository.findById(cardId)!!
        return Result.success(CardDTO.fromEntity(updated))
    }

    fun unlinkCard(cardId: String, userId: String): Boolean {
        val card = cardRepository.findById(cardId) ?: return false
        if (card.userId != userId) return false
        return cardRepository.update(cardId, mapOf(
            "userId" to null,
            "status" to "INACTIVE",
            "issuedAt" to null
        ))
    }
}
