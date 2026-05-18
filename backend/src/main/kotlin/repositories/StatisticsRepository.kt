package com.park.repositories

import com.park.database.tables.CardRequests
import com.park.database.tables.Cards
import com.park.database.tables.GamePlayLogs
import com.park.database.tables.Games
import com.park.database.tables.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant

/**
 * Game meta dùng cho filter/aggregate ở các endpoint statistics.
 * Bao gồm ticketPrice + category (so với version cũ private trong AdminService).
 */
data class GameMeta(
    val gameId: String,
    val name: String,
    val area: String?,
    val category: String?,
    val status: String,
    val ticketPrice: BigDecimal
)

data class RawPlayRow(
    val gameId: String,
    val userId: String,
    val playedAt: Instant,
    val amountCharged: BigDecimal
)

data class GameRow(
    val gameId: String,
    val name: String,
    val category: String?,
    val ticketPrice: BigDecimal,
    val durationMinutes: Int?
)

data class CardLifecycleCounts(
    val issued: Int,
    val blocked: Int,
    val pendingRequests: Int
)

interface IStatisticsRepository {
    fun queryPlayLogs(start: Instant, end: Instant, gameIds: Set<String>? = null): List<RawPlayRow>
    fun queryPlayLogsForGame(gameId: String, start: Instant, end: Instant): List<RawPlayRow>
    fun countNewUsers(start: Instant, end: Instant): Int
    /**
     * Trả Map<bucketKey, count> cho sparkline newUserValues theo bucket tự chọn.
     * `bucketKey` map từ `Users.createdAt` về một Instant đại diện đầu bucket.
     */
    fun newUserCountsByBucket(
        start: Instant,
        end: Instant,
        bucketKey: (Instant) -> Instant
    ): Map<Instant, Int>
    fun cardChannelCounts(start: Instant, end: Instant): Pair<Int, Int>
    fun cardLifecycleCounts(start: Instant, end: Instant): CardLifecycleCounts
    fun loadGameMetas(): List<GameMeta>
    fun findGame(gameId: String): GameRow?
}

class StatisticsRepository : IStatisticsRepository {

    override fun queryPlayLogs(
        start: Instant,
        end: Instant,
        gameIds: Set<String>?
    ): List<RawPlayRow> {
        if (gameIds != null && gameIds.isEmpty()) return emptyList()
        return transaction {
            val query = GamePlayLogs.selectAll().where {
                val timeCond = (GamePlayLogs.playedAt greaterEq start) and
                    (GamePlayLogs.playedAt less end)
                if (gameIds == null) timeCond
                else timeCond and (GamePlayLogs.gameId inList gameIds)
            }
            query.map {
                RawPlayRow(
                    gameId = it[GamePlayLogs.gameId],
                    userId = it[GamePlayLogs.userId],
                    playedAt = it[GamePlayLogs.playedAt],
                    amountCharged = it[GamePlayLogs.amountCharged]
                )
            }
        }
    }

    override fun queryPlayLogsForGame(
        gameId: String,
        start: Instant,
        end: Instant
    ): List<RawPlayRow> = transaction {
        GamePlayLogs.selectAll().where {
            (GamePlayLogs.gameId eq gameId) and
                (GamePlayLogs.playedAt greaterEq start) and
                (GamePlayLogs.playedAt less end)
        }.map {
            RawPlayRow(
                gameId = it[GamePlayLogs.gameId],
                userId = it[GamePlayLogs.userId],
                playedAt = it[GamePlayLogs.playedAt],
                amountCharged = it[GamePlayLogs.amountCharged]
            )
        }
    }

    override fun countNewUsers(start: Instant, end: Instant): Int = transaction {
        Users.selectAll().where {
            (Users.createdAt greaterEq start) and (Users.createdAt less end)
        }.count().toInt()
    }

    override fun newUserCountsByBucket(
        start: Instant,
        end: Instant,
        bucketKey: (Instant) -> Instant
    ): Map<Instant, Int> = transaction {
        Users.selectAll().where {
            (Users.createdAt greaterEq start) and (Users.createdAt less end)
        }.map { bucketKey(it[Users.createdAt]) }
            .groupingBy { it }
            .eachCount()
    }

    override fun cardChannelCounts(start: Instant, end: Instant): Pair<Int, Int> = transaction {
        val viaApp = CardRequests.selectAll().where {
            (CardRequests.status eq "COMPLETED") and
                (CardRequests.updatedAt greaterEq start) and
                (CardRequests.updatedAt less end)
        }.count().toInt()

        val totalIssued = Cards.selectAll().where {
            Cards.issuedAt.isNotNull() and
                (Cards.issuedAt greaterEq start) and
                (Cards.issuedAt less end)
        }.count().toInt()

        viaApp to (totalIssued - viaApp).coerceAtLeast(0)
    }

    override fun cardLifecycleCounts(start: Instant, end: Instant): CardLifecycleCounts = transaction {
        val issued = Cards.selectAll().where {
            Cards.issuedAt.isNotNull() and
                (Cards.issuedAt greaterEq start) and
                (Cards.issuedAt less end)
        }.count().toInt()

        val blocked = Cards.selectAll().where {
            Cards.blockedAt.isNotNull() and
                (Cards.blockedAt greaterEq start) and
                (Cards.blockedAt less end)
        }.count().toInt()

        val pending = CardRequests.selectAll().where {
            CardRequests.status eq "PENDING"
        }.count().toInt()

        CardLifecycleCounts(
            issued = issued,
            blocked = blocked,
            pendingRequests = pending
        )
    }

    override fun loadGameMetas(): List<GameMeta> = transaction {
        Games.selectAll().map {
            GameMeta(
                gameId = it[Games.gameId],
                name = it[Games.name],
                area = it[Games.location],
                category = it[Games.category],
                status = it[Games.status],
                ticketPrice = it[Games.pricePerTurn]
            )
        }
    }

    override fun findGame(gameId: String): GameRow? = transaction {
        Games.selectAll().where { Games.gameId eq gameId }.singleOrNull()?.let {
            GameRow(
                gameId = it[Games.gameId],
                name = it[Games.name],
                category = it[Games.category],
                ticketPrice = it[Games.pricePerTurn],
                durationMinutes = it[Games.durationMinutes]
            )
        }
    }
}
