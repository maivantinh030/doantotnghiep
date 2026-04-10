package com.park

import com.park.database.tables.BalanceTransactions
import com.park.database.tables.Cards
import com.park.database.tables.GamePlayLogs
import com.park.database.tables.Games
import com.park.database.tables.Users
import com.park.dto.NotificationDTO
import com.park.dto.SyncGamePlayRequest
import com.park.services.GameService
import com.park.services.NotificationService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

private class RecordingNotificationService : NotificationService() {
    val createdNotifications = mutableListOf<NotificationDTO>()

    override fun createNotification(
        userId: String,
        type: String,
        title: String,
        message: String,
        data: String?
    ): NotificationDTO {
        return NotificationDTO(
            notificationId = UUID.randomUUID().toString(),
            userId = userId,
            type = type,
            title = title,
            message = message,
            data = data,
            isRead = false,
            readAt = null,
            createdAt = Instant.now().toString()
        ).also { createdNotifications += it }
    }
}

class GameServiceSyncPlayTest {

    private lateinit var gameService: GameService
    private lateinit var notificationService: RecordingNotificationService

    @BeforeTest
    fun setUp() {
        Database.connect(
            url = "jdbc:h2:mem:${UUID.randomUUID()};MODE=MySQL;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )

        transaction {
            SchemaUtils.create(Users, Cards, Games, BalanceTransactions, GamePlayLogs)

            val createdAt = Instant.parse("2026-04-09T10:00:00Z")

            Users.insert {
                it[userId] = "user-1"
                it[accountId] = "account-1"
                it[fullName] = "Nguyen Van A"
                it[email] = "a@test.local"
                it[currentBalance] = BigDecimal("100000.00")
                it[avatarUrl] = null
                it[Users.createdAt] = createdAt
                it[updatedAt] = createdAt
            }

            Cards.insert {
                it[cardId] = "card-1"
                it[physicalCardUid] = "card-1"
                it[userId] = "user-1"
                it[cardName] = "The VIP"
                it[status] = "ACTIVE"
                it[depositAmount] = BigDecimal("0.00")
                it[depositStatus] = "NONE"
                it[issuedAt] = createdAt
                it[blockedAt] = null
                it[blockedReason] = null
                it[lastUsedAt] = null
                it[Cards.createdAt] = createdAt
                it[updatedAt] = createdAt
            }

            Games.insert {
                it[gameId] = "game-1"
                it[name] = "Tau luon toc do"
                it[description] = "Test game"
                it[shortDescription] = "Test game"
                it[category] = "Thrill"
                it[pricePerTurn] = BigDecimal("30000.00")
                it[durationMinutes] = 2
                it[location] = "Zone A"
                it[thumbnailUrl] = null
                it[galleryUrls] = null
                it[ageRequired] = null
                it[heightRequired] = null
                it[maxCapacity] = 10
                it[status] = "ACTIVE"
                it[riskLevel] = 3
                it[isFeatured] = false
                it[averageRating] = BigDecimal("0.0")
                it[totalReviews] = 0
                it[totalPlays] = 5
                it[Games.createdAt] = createdAt
                it[updatedAt] = createdAt
            }
        }

        notificationService = RecordingNotificationService()
        gameService = GameService(notificationService = notificationService)
    }

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(GamePlayLogs, BalanceTransactions, Games, Cards, Users)
        }
    }

    @Test
    fun `syncGamePlay is idempotent and mirrors card balance to the server ledger`() {
        val request = SyncGamePlayRequest(
            clientTransactionId = "client-tx-001",
            cardId = "card-1",
            chargedAmount = "30000",
            cardBalanceAfter = "70000",
            playedAt = "2026-04-09T10:15:30Z"
        )

        val first = gameService.syncGamePlay("game-1", request)
        val second = gameService.syncGamePlay("game-1", request)

        assertTrue(first.isSuccess)
        assertTrue(second.isSuccess)

        val firstResponse = first.getOrThrow()
        val secondResponse = second.getOrThrow()

        assertEquals(firstResponse.logId, secondResponse.logId)
        assertEquals(firstResponse.balanceTransactionId, secondResponse.balanceTransactionId)
        assertEquals("client-tx-001", secondResponse.clientTransactionId)
        assertMoneyTextEquals("70000", secondResponse.cardBalanceAfter)
        assertMoneyTextEquals("70000", secondResponse.balanceAfter)

        transaction {
            val userRow = Users.selectAll().single()
            val cardRow = Cards.selectAll().single()
            val gameRow = Games.selectAll().single()
            val logRow = GamePlayLogs.selectAll().single()
            val balanceRow = BalanceTransactions.selectAll().single()

            assertMoneyEquals("70000", userRow[Users.currentBalance])
            assertEquals(Instant.parse("2026-04-09T10:15:30Z"), cardRow[Cards.lastUsedAt])
            assertEquals(6, gameRow[Games.totalPlays])

            assertEquals("client-tx-001", logRow[GamePlayLogs.clientTransactionId])
            assertEquals("CARD", logRow[GamePlayLogs.method])
            assertMoneyEquals("30000", logRow[GamePlayLogs.amountCharged])
            assertMoneyEquals("70000", logRow[GamePlayLogs.cardBalanceAfter]!!)

            assertEquals("PAYMENT", balanceRow[BalanceTransactions.type])
            assertEquals("GAME_PLAY", balanceRow[BalanceTransactions.referenceType])
            assertEquals(firstResponse.logId, balanceRow[BalanceTransactions.referenceId])
            assertMoneyEquals("-30000", balanceRow[BalanceTransactions.amount])
            assertMoneyEquals("100000", balanceRow[BalanceTransactions.balanceBefore])
            assertMoneyEquals("70000", balanceRow[BalanceTransactions.balanceAfter])
        }

        assertEquals(1, notificationService.createdNotifications.size)
    }
}

private fun assertMoneyEquals(expected: String, actual: BigDecimal) {
    assertEquals(0, actual.compareTo(BigDecimal(expected)))
}

private fun assertMoneyTextEquals(expected: String, actual: String?) {
    require(actual != null) { "Expected money text but was null" }
    assertEquals(0, BigDecimal(actual).compareTo(BigDecimal(expected)))
}
