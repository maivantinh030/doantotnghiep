package com.park.repositories

import com.park.database.tables.CardRequests
import com.park.entities.CardRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface ICardRequestRepository {
    fun create(req: CardRequest): CardRequest
    fun findById(requestId: String): CardRequest?
    fun findByUserId(userId: String): List<CardRequest>
    fun findByStatus(status: String): List<CardRequest>
    fun update(requestId: String, updates: Map<String, Any?>): Boolean
}

class CardRequestRepository : ICardRequestRepository {

    override fun create(req: CardRequest): CardRequest {
        return transaction {
            CardRequests.insert {
                it[requestId] = req.requestId
                it[userId] = req.userId
                it[status] = req.status
                it[depositPaidOnline] = req.depositPaidOnline
                it[depositAmount] = req.depositAmount
                it[note] = req.note
                it[approvedBy] = req.approvedBy
                it[createdAt] = req.createdAt
                it[updatedAt] = req.updatedAt
            }
            req
        }
    }

    override fun findById(requestId: String): CardRequest? {
        return transaction {
            CardRequests.selectAll().where { CardRequests.requestId eq requestId }
                .singleOrNull()?.let { mapRow(it) }
        }
    }

    override fun findByUserId(userId: String): List<CardRequest> {
        return transaction {
            CardRequests.selectAll().where { CardRequests.userId eq userId }
                .orderBy(CardRequests.createdAt, SortOrder.DESC)
                .map { mapRow(it) }
        }
    }

    override fun findByStatus(status: String): List<CardRequest> {
        return transaction {
            CardRequests.selectAll().where { CardRequests.status eq status }
                .orderBy(CardRequests.createdAt, SortOrder.ASC)
                .map { mapRow(it) }
        }
    }

    override fun update(requestId: String, updates: Map<String, Any?>): Boolean {
        return transaction {
            CardRequests.update(where = { CardRequests.requestId eq requestId }) { stmt ->
                updates.forEach { (key, value) ->
                    when (key) {
                        "status" -> stmt[status] = value as String
                        "approvedBy" -> stmt[approvedBy] = value as? String
                        "note" -> stmt[note] = value as? String
                    }
                }
                stmt[updatedAt] = Instant.now()
            } > 0
        }
    }

    private fun mapRow(row: ResultRow): CardRequest {
        return CardRequest(
            requestId = row[CardRequests.requestId],
            userId = row[CardRequests.userId],
            status = row[CardRequests.status],
            depositPaidOnline = row[CardRequests.depositPaidOnline],
            depositAmount = row[CardRequests.depositAmount],
            note = row[CardRequests.note],
            approvedBy = row[CardRequests.approvedBy],
            createdAt = row[CardRequests.createdAt],
            updatedAt = row[CardRequests.updatedAt]
        )
    }
}
