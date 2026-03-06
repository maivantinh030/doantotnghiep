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

    fun createVirtualOnlyCard(userId: String): Result<CardDTO> {
        // Kiểm tra user đã có thẻ chưa
        val existing = cardRepository.findByUserId(userId)
        if (existing.isNotEmpty()) {
            return Result.failure(IllegalStateException("Tài khoản đã có thẻ liên kết"))
        }

        // Sinh virtual UID unique
        var virtualUid: String
        do {
            virtualUid = (1..7).joinToString("") { "%02X".format((0..255).random()) }
        } while (cardRepository.findByVirtualUid(virtualUid) != null)

        val now = Instant.now()
        val card = Card(
            cardId = UUID.randomUUID().toString(),
            physicalCardUid = null,
            virtualCardUid = virtualUid,
            cardType = "VIRTUAL",
            userId = userId,
            cardName = null,
            status = "ACTIVE",
            pinHash = null,
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

        // Kiểm tra user đã có thẻ ảo chưa (VIRTUAL-only) → liên kết thẻ vật lý vào thẻ ảo đó
        val userCards = cardRepository.findByUserId(userId)
        val virtualCard = userCards.firstOrNull { it.cardType == "VIRTUAL" }
        if (virtualCard != null) {
            val updates = mutableMapOf<String, Any?>(
                "physicalCardUid" to request.physicalCardUid,
                "cardType" to "BOTH"
            )
            request.cardName?.let { updates["cardName"] = it }
            cardRepository.update(virtualCard.cardId, updates)
            val updated = cardRepository.findById(virtualCard.cardId)!!
            return Result.success(CardDTO.fromEntity(updated))
        }

        // Tạo thẻ mới (PHYSICAL)
        val now = Instant.now()
        val card = Card(
            cardId = UUID.randomUUID().toString(),
            physicalCardUid = request.physicalCardUid,
            virtualCardUid = null,
            cardType = "PHYSICAL",
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

    fun generateVirtualCard(cardId: String, userId: String): Result<CardDTO> {
        val card = cardRepository.findById(cardId)
            ?: return Result.failure(NoSuchElementException("Thẻ không tồn tại"))
        if (card.userId != userId) {
            return Result.failure(IllegalAccessException("Không có quyền thao tác thẻ này"))
        }
        if (card.status != "ACTIVE") {
            return Result.failure(IllegalStateException("Thẻ phải đang hoạt động để tạo thẻ ảo"))
        }
        if (card.virtualCardUid != null) {
            return Result.failure(IllegalStateException("Thẻ ảo đã được tạo cho thẻ này"))
        }

        // Sinh virtual UID: 7 bytes hex (chuẩn NFC UID type 4), đảm bảo unique
        var virtualUid: String
        do {
            virtualUid = (1..7).joinToString("") { "%02X".format((0..255).random()) }
        } while (cardRepository.findByVirtualUid(virtualUid) != null)

        cardRepository.update(cardId, mapOf(
            "virtualCardUid" to virtualUid,
            "cardType" to "BOTH"
        ))
        val updated = cardRepository.findById(cardId)!!
        return Result.success(CardDTO.fromEntity(updated))
    }

    fun removeVirtualCard(cardId: String, userId: String): Result<CardDTO> {
        val card = cardRepository.findById(cardId)
            ?: return Result.failure(NoSuchElementException("Thẻ không tồn tại"))
        if (card.userId != userId) {
            return Result.failure(IllegalAccessException("Không có quyền thao tác thẻ này"))
        }
        if (card.virtualCardUid == null) {
            return Result.failure(IllegalStateException("Thẻ này chưa có thẻ ảo"))
        }

        if (card.cardType == "VIRTUAL") {
            // Thẻ ảo thuần túy → xóa toàn bộ bản ghi
            cardRepository.delete(cardId)
            // Trả về bản sao với virtualCardUid = null để frontend biết đã xóa
            return Result.success(CardDTO.fromEntity(card.copy(virtualCardUid = null)))
        }

        // Thẻ BOTH → chỉ xóa virtualCardUid, giữ lại thẻ vật lý
        cardRepository.update(cardId, mapOf(
            "virtualCardUid" to null,
            "cardType" to "PHYSICAL"
        ))
        val updated = cardRepository.findById(cardId)!!
        return Result.success(CardDTO.fromEntity(updated))
    }
}
