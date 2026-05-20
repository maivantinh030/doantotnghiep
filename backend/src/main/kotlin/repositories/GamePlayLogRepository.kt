package com.park.repositories

import com.park.database.tables.GamePlayLogs
import com.park.database.tables.Games
import com.park.dto.GamePlayHistoryDTO
import com.park.entities.GamePlayLog
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

interface IGamePlayLogRepository {
    fun create(log: GamePlayLog): GamePlayLog
    fun existsByUserAndGame(userId: String, gameId: String): Boolean
    fun findByClientTransactionId(clientTransactionId: String): GamePlayLog?
    fun findGameIdsByUser(userId: String): List<String>
    fun countByUser(userId: String): Long
    fun findHistoryByUser(userId: String, limit: Int, offset: Long): List<GamePlayHistoryDTO>
    fun findAllByUser(userId: String): List<GamePlayLog>
}

class GamePlayLogRepository : IGamePlayLogRepository {

    override fun create(log: GamePlayLog): GamePlayLog {
        return transaction {
            GamePlayLogs.insert {
                it[logId] = log.logId
                it[clientTransactionId] = log.clientTransactionId
                it[userId] = log.userId
                it[gameId] = log.gameId
                it[cardId] = log.cardId
                it[method] = log.method
                it[amountCharged] = log.amountCharged
                it[cardBalanceAfter] = log.cardBalanceAfter
                it[playedAt] = log.playedAt
            }
            log
        }
    }

    override fun existsByUserAndGame(userId: String, gameId: String): Boolean {
        return transaction {
            GamePlayLogs.selectAll().where {
                (GamePlayLogs.userId eq userId) and (GamePlayLogs.gameId eq gameId)
            }.count() > 0
        }
    }

    override fun findByClientTransactionId(clientTransactionId: String): GamePlayLog? {
        return transaction {
            GamePlayLogs.selectAll()
                .where { GamePlayLogs.clientTransactionId eq clientTransactionId }
                .singleOrNull()
                ?.toEntity()
        }
    }

    override fun findGameIdsByUser(userId: String): List<String> {
        return transaction {
            GamePlayLogs.select(GamePlayLogs.gameId)
                .where { GamePlayLogs.userId eq userId }
                .map { it[GamePlayLogs.gameId] }
        }
    }

    override fun countByUser(userId: String): Long {
        return transaction {
            GamePlayLogs.selectAll()
                .where { GamePlayLogs.userId eq userId }
                .count()
        }
    }

    override fun findAllByUser(userId: String): List<GamePlayLog> {
        return transaction {
            GamePlayLogs.selectAll()
                .where { GamePlayLogs.userId eq userId }
                .map { it.toEntity() }
        }
    }

    override fun findHistoryByUser(userId: String, limit: Int, offset: Long): List<GamePlayHistoryDTO> {
        return transaction {
            GamePlayLogs
                .join(Games, JoinType.INNER, GamePlayLogs.gameId, Games.gameId)
                .selectAll()
                .where { GamePlayLogs.userId eq userId }
                .orderBy(GamePlayLogs.playedAt, SortOrder.DESC)
                .limit(limit)
                .offset(offset)
                .map { row ->
                    GamePlayHistoryDTO(
                        logId = row[GamePlayLogs.logId],
                        gameId = row[GamePlayLogs.gameId],
                        gameName = row[Games.name],
                        gameCategory = row[Games.category],
                        gameThumbnailUrl = row[Games.thumbnailUrl],
                        cardId = row[GamePlayLogs.cardId],
                        method = row[GamePlayLogs.method],
                        amountCharged = row[GamePlayLogs.amountCharged].toString(),
                        cardBalanceAfter = row[GamePlayLogs.cardBalanceAfter]?.toString(),
                        playedAt = row[GamePlayLogs.playedAt].toString()
                    )
                }
        }
    }

    private fun ResultRow.toEntity(): GamePlayLog {
        return GamePlayLog(
            logId = this[GamePlayLogs.logId],
            clientTransactionId = this[GamePlayLogs.clientTransactionId],
            userId = this[GamePlayLogs.userId],
            gameId = this[GamePlayLogs.gameId],
            cardId = this[GamePlayLogs.cardId],
            method = this[GamePlayLogs.method],
            amountCharged = this[GamePlayLogs.amountCharged],
            cardBalanceAfter = this[GamePlayLogs.cardBalanceAfter],
            playedAt = this[GamePlayLogs.playedAt]
        )
    }
}
