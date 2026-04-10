package com.park.services

import com.park.database.tables.BalanceTransactions
import com.park.database.tables.Cards
import com.park.database.tables.GamePlayLogs
import com.park.database.tables.Games
import com.park.database.tables.Users
import com.park.dto.*
import com.park.entities.Card
import com.park.entities.Game
import com.park.entities.User
import com.park.repositories.CardRepository
import com.park.repositories.GameRepository
import com.park.repositories.ICardRepository
import com.park.repositories.IGameRepository
import com.park.repositories.IUserRepository
import com.park.repositories.UserRepository
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class GameService(
    private val gameRepository: IGameRepository = GameRepository(),
    private val cardRepository: ICardRepository = CardRepository(),
    private val userRepository: IUserRepository = UserRepository(),
    private val notificationService: NotificationService = NotificationService()
) {

    fun getGames(page: Int, size: Int, category: String?, search: String?): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()

        val games: List<Game>
        val total: Long

        when {
            !search.isNullOrBlank() -> {
                games = gameRepository.search(search, size, offset)
                total = gameRepository.countBySearch(search)
            }
            !category.isNullOrBlank() -> {
                games = gameRepository.findByCategory(category, size, offset)
                total = gameRepository.countByCategory(category)
            }
            else -> {
                games = gameRepository.findAll(size, offset)
                total = gameRepository.countAll()
            }
        }

        return mapOf(
            "items" to games.map { GameListItemDTO.fromEntity(it) },
            "total" to total,
            "page" to page,
            "size" to size,
            "totalPages" to ((total + size - 1) / size)
        )
    }

    fun getGameById(gameId: String): GameDTO? {
        val game = gameRepository.findById(gameId) ?: return null
        return GameDTO.fromEntity(game)
    }

    fun getFeaturedGames(limit: Int): List<GameListItemDTO> {
        return gameRepository.findFeatured(limit).map { GameListItemDTO.fromEntity(it) }
    }

    fun getCategories(): List<String> {
        return gameRepository.findAllCategories()
    }

    fun createGame(request: CreateGameRequest): Result<GameDTO> {
        val errors = validateCreateGame(request)
        if (errors.isNotEmpty()) {
            return Result.failure(IllegalArgumentException(errors.values.first()))
        }

        val now = Instant.now()
        val game = Game(
            gameId = UUID.randomUUID().toString(),
            name = request.name,
            description = request.description,
            shortDescription = request.shortDescription,
            category = request.category,
            pricePerTurn = BigDecimal(request.pricePerTurn),
            durationMinutes = request.durationMinutes,
            location = request.location,
            thumbnailUrl = request.thumbnailUrl,
            galleryUrls = request.galleryUrls?.let { "[${it.joinToString(",") { url -> "\"$url\"" }}]" },
            ageRequired = request.ageRequired,
            heightRequired = request.heightRequired,
            maxCapacity = request.maxCapacity,
            status = "ACTIVE",
            riskLevel = request.riskLevel,
            isFeatured = request.isFeatured,
            averageRating = BigDecimal("0.0"),
            totalReviews = 0,
            totalPlays = 0,
            createdAt = now,
            updatedAt = now
        )

        return Result.success(GameDTO.fromEntity(gameRepository.create(game)))
    }

    fun updateGame(gameId: String, request: UpdateGameRequest): Result<GameDTO> {
        val existing = gameRepository.findById(gameId)
            ?: return Result.failure(NoSuchElementException("Game khong ton tai"))

        if (request.status != null && request.status !in listOf("ACTIVE", "MAINTENANCE", "CLOSED")) {
            return Result.failure(IllegalArgumentException("Status phai la ACTIVE, MAINTENANCE hoac CLOSED"))
        }
        if (request.riskLevel != null && (request.riskLevel < 1 || request.riskLevel > 5)) {
            return Result.failure(IllegalArgumentException("Risk level phai tu 1 den 5"))
        }

        val updates = mutableMapOf<String, Any?>()
        request.name?.let { updates["name"] = it }
        request.description?.let { updates["description"] = it }
        request.shortDescription?.let { updates["shortDescription"] = it }
        request.category?.let { updates["category"] = it }
        request.pricePerTurn?.let { updates["pricePerTurn"] = BigDecimal(it) }
        request.durationMinutes?.let { updates["durationMinutes"] = it }
        request.location?.let { updates["location"] = it }
        request.thumbnailUrl?.let { updates["thumbnailUrl"] = it }
        request.galleryUrls?.let { urls ->
            updates["galleryUrls"] = "[${urls.joinToString(",") { "\"$it\"" }}]"
        }
        request.ageRequired?.let { updates["ageRequired"] = it }
        request.heightRequired?.let { updates["heightRequired"] = it }
        request.maxCapacity?.let { updates["maxCapacity"] = it }
        request.status?.let { updates["status"] = it }
        request.riskLevel?.let { updates["riskLevel"] = it }
        request.isFeatured?.let { updates["isFeatured"] = it }

        if (updates.isEmpty()) {
            return Result.success(GameDTO.fromEntity(existing))
        }

        gameRepository.update(gameId, updates)
        return Result.success(GameDTO.fromEntity(gameRepository.findById(gameId)!!))
    }

    fun useGame(gameId: String, request: UseGameRequest): Result<UseGameResponse> {
        val game = gameRepository.findById(gameId)
            ?: return Result.failure(NoSuchElementException("Game khong ton tai"))
        if (game.status != "ACTIVE") {
            return Result.failure(IllegalStateException("Game hien khong hoat dong"))
        }

        val card = try {
            resolveCard(request.cardId, request.cardUid, requireActiveCard = true)
                ?: return Result.failure(IllegalArgumentException("Thieu cardId/cardUid de xu ly luot choi"))
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val user = try {
            resolveUserForCard(card)
                ?: return Result.failure(IllegalStateException("The chua duoc lien ket voi tai khoan"))
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val amount = game.pricePerTurn
        if (amount <= BigDecimal.ZERO) {
            return Result.failure(IllegalStateException("Game chua co gia hop le"))
        }
        if (user.currentBalance < amount) {
            return Result.failure(IllegalStateException("So du khong du"))
        }

        val now = Instant.now()
        val balanceBefore = user.currentBalance
        val balanceAfter = balanceBefore.subtract(amount)
        val logId = UUID.randomUUID().toString()
        val txId = UUID.randomUUID().toString()

        transaction {
            Users.update({ Users.userId eq user.userId }) {
                it[Users.currentBalance] = balanceAfter
                it[Users.updatedAt] = now
            }

            BalanceTransactions.insert {
                it[BalanceTransactions.transactionId] = txId
                it[BalanceTransactions.userId] = user.userId
                it[BalanceTransactions.amount] = amount.negate()
                it[BalanceTransactions.balanceBefore] = balanceBefore
                it[BalanceTransactions.balanceAfter] = balanceAfter
                it[BalanceTransactions.type] = "PAYMENT"
                it[BalanceTransactions.referenceType] = "GAME_PLAY"
                it[BalanceTransactions.referenceId] = logId
                it[BalanceTransactions.description] = "Choi game ${game.name}"
                it[BalanceTransactions.createdAt] = now
                it[BalanceTransactions.createdBy] = null
            }

            GamePlayLogs.insert {
                it[GamePlayLogs.logId] = logId
                it[GamePlayLogs.clientTransactionId] = null
                it[GamePlayLogs.userId] = user.userId
                it[GamePlayLogs.gameId] = gameId
                it[GamePlayLogs.cardId] = card.cardId
                it[GamePlayLogs.method] = "BALANCE"
                it[GamePlayLogs.amountCharged] = amount
                it[GamePlayLogs.cardBalanceAfter] = balanceAfter
                it[GamePlayLogs.playedAt] = now
            }

            Cards.update({ Cards.cardId eq card.cardId }) {
                it[Cards.lastUsedAt] = now
                it[Cards.updatedAt] = now
            }

            Games.update({ Games.gameId eq gameId }) {
                it[Games.totalPlays] = game.totalPlays + 1
                it[Games.updatedAt] = now
            }
        }

        createGamePlayNotification(
            userId = user.userId,
            game = game,
            logId = logId,
            cardId = card.cardId,
            amount = amount,
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            playedAt = now
        )

        return Result.success(
            UseGameResponse(
                logId = logId,
                gameId = gameId,
                userId = user.userId,
                cardId = card.cardId,
                chargedAmount = amount.toString(),
                balanceBefore = balanceBefore.toString(),
                balanceAfter = balanceAfter.toString(),
                cardBalanceAfter = balanceAfter.toString(),
                balanceTransactionId = txId,
                playedAt = now.toString()
            )
        )
    }

    fun syncGamePlay(gameId: String, request: SyncGamePlayRequest): Result<UseGameResponse> {
        val clientTransactionId = request.clientTransactionId.trim()
        if (clientTransactionId.isBlank()) {
            return Result.failure(IllegalArgumentException("clientTransactionId khong hop le"))
        }

        findExistingSyncResult(clientTransactionId)?.let { return Result.success(it) }

        val game = gameRepository.findById(gameId)
            ?: return Result.failure(NoSuchElementException("Game khong ton tai"))

        val card = cardRepository.findById(request.cardId.trim())
            ?: return Result.failure(NoSuchElementException("Khong tim thay the voi cardId: ${request.cardId}"))

        val user = try {
            resolveUserForCard(card)
                ?: return Result.failure(IllegalStateException("The chua duoc lien ket voi tai khoan"))
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val chargedAmount = parsePositiveAmount(request.chargedAmount, "chargedAmount")
            ?: return Result.failure(IllegalArgumentException("chargedAmount khong hop le"))
        val cardBalanceAfter = parseNonNegativeAmount(request.cardBalanceAfter, "cardBalanceAfter")
            ?: return Result.failure(IllegalArgumentException("cardBalanceAfter khong hop le"))
        val playedAt = runCatching { Instant.parse(request.playedAt) }.getOrElse {
            return Result.failure(IllegalArgumentException("playedAt khong hop le"))
        }

        val balanceBefore = cardBalanceAfter.add(chargedAmount)
        val logId = UUID.randomUUID().toString()
        val txId = UUID.randomUUID().toString()
        val syncNow = Instant.now()

        transaction {
            Users.update({ Users.userId eq user.userId }) {
                it[Users.currentBalance] = cardBalanceAfter
                it[Users.updatedAt] = syncNow
            }

            BalanceTransactions.insert {
                it[BalanceTransactions.transactionId] = txId
                it[BalanceTransactions.userId] = user.userId
                it[BalanceTransactions.amount] = chargedAmount.negate()
                it[BalanceTransactions.balanceBefore] = balanceBefore
                it[BalanceTransactions.balanceAfter] = cardBalanceAfter
                it[BalanceTransactions.type] = "PAYMENT"
                it[BalanceTransactions.referenceType] = "GAME_PLAY"
                it[BalanceTransactions.referenceId] = logId
                it[BalanceTransactions.description] = "Dong bo luot choi ${game.name}"
                it[BalanceTransactions.createdAt] = playedAt
                it[BalanceTransactions.createdBy] = null
            }

            GamePlayLogs.insert {
                it[GamePlayLogs.logId] = logId
                it[GamePlayLogs.clientTransactionId] = clientTransactionId
                it[GamePlayLogs.userId] = user.userId
                it[GamePlayLogs.gameId] = gameId
                it[GamePlayLogs.cardId] = card.cardId
                it[GamePlayLogs.method] = "CARD"
                it[GamePlayLogs.amountCharged] = chargedAmount
                it[GamePlayLogs.cardBalanceAfter] = cardBalanceAfter
                it[GamePlayLogs.playedAt] = playedAt
            }

            Cards.update({ Cards.cardId eq card.cardId }) {
                it[Cards.lastUsedAt] = playedAt
                it[Cards.updatedAt] = syncNow
            }

            Games.update({ Games.gameId eq gameId }) {
                it[Games.totalPlays] = game.totalPlays + 1
                it[Games.updatedAt] = syncNow
            }
        }

        createGamePlayNotification(
            userId = user.userId,
            game = game,
            logId = logId,
            cardId = card.cardId,
            amount = chargedAmount,
            balanceBefore = balanceBefore,
            balanceAfter = cardBalanceAfter,
            playedAt = playedAt
        )

        return Result.success(
            UseGameResponse(
                logId = logId,
                gameId = gameId,
                userId = user.userId,
                cardId = card.cardId,
                clientTransactionId = clientTransactionId,
                chargedAmount = chargedAmount.toString(),
                balanceBefore = balanceBefore.toString(),
                balanceAfter = cardBalanceAfter.toString(),
                cardBalanceAfter = cardBalanceAfter.toString(),
                balanceTransactionId = txId,
                playedAt = playedAt.toString()
            )
        )
    }

    fun deleteGame(gameId: String): Boolean {
        gameRepository.findById(gameId) ?: return false
        return gameRepository.delete(gameId)
    }

    private fun resolveCard(cardId: String?, cardUid: String?, requireActiveCard: Boolean): Card? {
        val normalizedCardId = cardId?.trim().takeUnless { it.isNullOrBlank() }
        val normalizedCardUid = cardUid?.trim().takeUnless { it.isNullOrBlank() }

        val card = when {
            normalizedCardId != null -> cardRepository.findById(normalizedCardId)
                ?: throw NoSuchElementException("Khong tim thay the voi cardId: $normalizedCardId")
            normalizedCardUid != null -> cardRepository.findByPhysicalUid(normalizedCardUid)
                ?: throw NoSuchElementException("Khong tim thay the voi UID: $normalizedCardUid")
            else -> return null
        }

        if (requireActiveCard && card.status != "ACTIVE") {
            throw IllegalStateException("The khong hoat dong (trang thai: ${card.status})")
        }
        return card
    }

    private fun resolveUserForCard(card: Card): User? {
        val userId = card.userId ?: return null
        return userRepository.findById(userId)
            ?: throw NoSuchElementException("Khong tim thay user lien ket voi the")
    }

    private fun parsePositiveAmount(raw: String, fieldName: String): BigDecimal? {
        val amount = runCatching { BigDecimal(raw.trim()) }.getOrNull() ?: return null
        if (amount <= BigDecimal.ZERO) return null
        return amount
    }

    private fun parseNonNegativeAmount(raw: String, fieldName: String): BigDecimal? {
        val amount = runCatching { BigDecimal(raw.trim()) }.getOrNull() ?: return null
        if (amount < BigDecimal.ZERO) return null
        return amount
    }

    private fun findExistingSyncResult(clientTransactionId: String): UseGameResponse? {
        return transaction {
            val logRow = GamePlayLogs.selectAll()
                .where { GamePlayLogs.clientTransactionId eq clientTransactionId }
                .singleOrNull()
                ?: return@transaction null

            val logId = logRow[GamePlayLogs.logId]
            val txRow = BalanceTransactions.selectAll()
                .where {
                    (BalanceTransactions.referenceId eq logId) and
                        (BalanceTransactions.referenceType eq "GAME_PLAY")
                }
                .orderBy(BalanceTransactions.createdAt, SortOrder.DESC)
                .limit(1)
                .singleOrNull()

            UseGameResponse(
                logId = logId,
                gameId = logRow[GamePlayLogs.gameId],
                userId = logRow[GamePlayLogs.userId],
                cardId = logRow[GamePlayLogs.cardId].orEmpty(),
                clientTransactionId = logRow[GamePlayLogs.clientTransactionId],
                chargedAmount = logRow[GamePlayLogs.amountCharged].toString(),
                balanceBefore = txRow?.get(BalanceTransactions.balanceBefore)?.toString(),
                balanceAfter = txRow?.get(BalanceTransactions.balanceAfter)?.toString()
                    ?: logRow[GamePlayLogs.cardBalanceAfter]?.toString(),
                cardBalanceAfter = logRow[GamePlayLogs.cardBalanceAfter]?.toString(),
                balanceTransactionId = txRow?.get(BalanceTransactions.transactionId),
                playedAt = logRow[GamePlayLogs.playedAt].toString()
            )
        }
    }

    private fun createGamePlayNotification(
        userId: String,
        game: Game,
        logId: String,
        cardId: String,
        amount: BigDecimal,
        balanceBefore: BigDecimal,
        balanceAfter: BigDecimal,
        playedAt: Instant
    ) {
        try {
            val amountText = amount.stripTrailingZeros().toPlainString()
            val balanceBeforeText = balanceBefore.stripTrailingZeros().toPlainString()
            val balanceAfterText = balanceAfter.stripTrailingZeros().toPlainString()
            notificationService.createNotification(
                userId = userId,
                type = "GAME",
                title = "Ban vua choi ${game.name}",
                message = "Da tru $amountText VND cho luot choi ${game.name}. So du con lai: $balanceAfterText VND.",
                data = NotificationDataCodec.encode(
                    GamePlayNotificationData(
                        gameId = game.gameId,
                        gameName = game.name,
                        logId = logId,
                        cardId = cardId,
                        chargedAmount = amountText,
                        balanceBefore = balanceBeforeText,
                        balanceAfter = balanceAfterText,
                        playedAt = playedAt.toString()
                    )
                )
            )
        } catch (e: Exception) {
            println("Warning: khong tao duoc notification game play: ${e.message}")
        }
    }

    private fun validateCreateGame(request: CreateGameRequest): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (request.name.isBlank()) {
            errors["name"] = "Ten tro choi khong duoc de trong"
        }

        try {
            val price = BigDecimal(request.pricePerTurn)
            if (price <= BigDecimal.ZERO) {
                errors["pricePerTurn"] = "Gia ve phai lon hon 0"
            }
        } catch (_: NumberFormatException) {
            errors["pricePerTurn"] = "Gia ve khong hop le"
        }

        if (request.riskLevel != null && (request.riskLevel < 1 || request.riskLevel > 5)) {
            errors["riskLevel"] = "Risk level phai tu 1 den 5"
        }

        return errors
    }
}
