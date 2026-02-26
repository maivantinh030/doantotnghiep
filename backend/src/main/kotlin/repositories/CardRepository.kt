package com.park.repositories

import com.park.database.tables.Cards
import com.park.entities.Card
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

interface ICardRepository {
    fun create(card: Card): Card
    fun findById(cardId: String): Card?
    fun findByPhysicalUid(uid: String): Card?
    fun findByUserId(userId: String): List<Card>
    fun update(cardId: String, updates: Map<String, Any?>): Boolean
    fun delete(cardId: String): Boolean
}

class CardRepository : ICardRepository {

    override fun create(card: Card): Card {
        return transaction {
            Cards.insert {
                it[cardId] = card.cardId
                it[physicalCardUid] = card.physicalCardUid
                it[userId] = card.userId
                it[cardName] = card.cardName
                it[status] = card.status
                it[pinHash] = card.pinHash
                it[issuedAt] = card.issuedAt
                it[createdAt] = card.createdAt
                it[updatedAt] = card.updatedAt
            }
            card
        }
    }

    override fun findById(cardId: String): Card? {
        return transaction {
            Cards.selectAll().where { Cards.cardId eq cardId }
                .singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun findByPhysicalUid(uid: String): Card? {
        return transaction {
            Cards.selectAll().where { Cards.physicalCardUid eq uid }
                .singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun findByUserId(userId: String): List<Card> {
        return transaction {
            Cards.selectAll().where { Cards.userId eq userId }
                .orderBy(Cards.createdAt, SortOrder.DESC)
                .map { mapRow(it) }
        }
    }

    override fun update(cardId: String, updates: Map<String, Any?>): Boolean {
        return transaction {
            Cards.update(where = { Cards.cardId eq cardId }) { stmt ->
                updates.forEach { (key, value) ->
                    when (key) {
                        "userId" -> stmt[userId] = value as? String
                        "cardName" -> stmt[cardName] = value as? String
                        "status" -> stmt[status] = value as String
                        "pinHash" -> stmt[pinHash] = value as? String
                        "issuedAt" -> stmt[issuedAt] = value as? Instant
                        "blockedAt" -> stmt[blockedAt] = value as? Instant
                        "blockedReason" -> stmt[blockedReason] = value as? String
                        "lastUsedAt" -> stmt[lastUsedAt] = value as? Instant
                    }
                }
                stmt[updatedAt] = Instant.now()
            } > 0
        }
    }

    override fun delete(cardId: String): Boolean {
        return transaction {
            Cards.deleteWhere { Cards.cardId eq cardId } > 0
        }
    }

    private fun mapRow(row: ResultRow): Card {
        return Card(
            cardId = row[Cards.cardId],
            physicalCardUid = row[Cards.physicalCardUid],
            userId = row[Cards.userId],
            cardName = row[Cards.cardName],
            status = row[Cards.status],
            pinHash = row[Cards.pinHash],
            issuedAt = row[Cards.issuedAt],
            blockedAt = row[Cards.blockedAt],
            blockedReason = row[Cards.blockedReason],
            lastUsedAt = row[Cards.lastUsedAt],
            createdAt = row[Cards.createdAt],
            updatedAt = row[Cards.updatedAt]
        )
    }
}
