package com.park.services

import com.park.database.tables.BalanceTransactions
import com.park.database.tables.Cards
import com.park.database.tables.GamePlayLogs
import com.park.database.tables.Games
import com.park.database.tables.Users
import com.park.dto.*
import com.park.entities.Game
import com.park.repositories.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Service xử lý business logic cho Game
 */
class GameService(
    private val gameRepository: IGameRepository = GameRepository(),
    private val cardRepository: ICardRepository = CardRepository(),
    private val userRepository: IUserRepository = UserRepository(),
    private val notificationService: NotificationService = NotificationService()
) {

    /**
     * Lấy danh sách game (có phân trang)
     */
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

    /**
     * Lấy chi tiết game
     */
    fun getGameById(gameId: String): GameDTO? {
        val game = gameRepository.findById(gameId) ?: return null
        return GameDTO.fromEntity(game)
    }

    /**
     * Lấy danh sách game nổi bật
     */
    fun getFeaturedGames(limit: Int): List<GameListItemDTO> {
        return gameRepository.findFeatured(limit).map { GameListItemDTO.fromEntity(it) }
    }

    /**
     * Lấy danh sách categories
     */
    fun getCategories(): List<String> {
        return gameRepository.findAllCategories()
    }

    /**
     * Tạo game mới (Admin only)
     */
    fun createGame(request: CreateGameRequest): Result<GameDTO> {
        // Validate
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

        val created = gameRepository.create(game)
        return Result.success(GameDTO.fromEntity(created))
    }

    /**
     * Cập nhật game (Admin only)
     */
    fun updateGame(gameId: String, request: UpdateGameRequest): Result<GameDTO> {
        val existing = gameRepository.findById(gameId)
            ?: return Result.failure(NoSuchElementException("Game không tồn tại"))

        // Validate status nếu có
        if (request.status != null && request.status !in listOf("ACTIVE", "MAINTENANCE", "CLOSED")) {
            return Result.failure(IllegalArgumentException("Status phải là ACTIVE, MAINTENANCE hoặc CLOSED"))
        }

        // Validate riskLevel nếu có
        if (request.riskLevel != null && (request.riskLevel < 1 || request.riskLevel > 5)) {
            return Result.failure(IllegalArgumentException("Risk level phải từ 1 đến 5"))
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

        val updated = gameRepository.findById(gameId)!!
        return Result.success(GameDTO.fromEntity(updated))
    }

    /**
     * Sử dụng game - terminal quét NFC card → tìm vé hợp lệ → trừ 1 lượt
     * POST /api/games/{gameId}/play
     */
    fun useGame(gameId: String, request: UseGameRequest): Result<UseGameResponse> {
        val game = gameRepository.findById(gameId)
            ?: return Result.failure(NoSuchElementException("Game khong ton tai"))

        if (game.status != "ACTIVE") {
            return Result.failure(IllegalStateException("Game hien khong hoat dong"))
        }

        val normalizedCardId = request.cardId?.trim().takeUnless { it.isNullOrBlank() }
        val normalizedCardUid = request.cardUid?.trim().takeUnless { it.isNullOrBlank() }

        val card = when {
            normalizedCardId != null -> {
                cardRepository.findById(normalizedCardId)
                    ?: return Result.failure(NoSuchElementException("Khong tim thay the voi cardId: $normalizedCardId"))
            }

            normalizedCardUid != null -> {
                cardRepository.findByPhysicalUid(normalizedCardUid)
                    ?: return Result.failure(NoSuchElementException("Khong tim thay the voi UID: $normalizedCardUid"))
            }

            else -> {
                return Result.failure(IllegalArgumentException("Thieu cardId/cardUid de xu ly luot choi"))
            }
        }

        if (card.status != "ACTIVE") {
            return Result.failure(IllegalStateException("The khong hoat dong (trang thai: ${card.status})"))
        }

        val userId = card.userId
            ?: return Result.failure(IllegalStateException("The chua duoc lien ket voi tai khoan"))

        val user = userRepository.findById(userId)
            ?: return Result.failure(NoSuchElementException("Khong tim thay user lien ket voi the"))

        val amount = game.pricePerTurn
        if (amount <= BigDecimal.ZERO) {
            return Result.failure(IllegalStateException("Game chua co gia hop le"))
        }

        val balanceBefore = user.currentBalance
        if (balanceBefore < amount) {
            return Result.failure(IllegalStateException("So du khong du"))
        }
        val balanceAfter = balanceBefore.subtract(amount)

        val now = Instant.now()
        val logId = UUID.randomUUID().toString()
        val txId = UUID.randomUUID().toString()

        transaction {
            Users.update({ Users.userId eq userId }) {
                it[Users.currentBalance] = balanceAfter
                it[Users.updatedAt] = now
            }

            BalanceTransactions.insert {
                it[BalanceTransactions.transactionId] = txId
                it[BalanceTransactions.userId] = userId
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
                it[GamePlayLogs.userId] = userId
                it[GamePlayLogs.gameId] = gameId
                it[GamePlayLogs.cardId] = card.cardId
                it[GamePlayLogs.method] = "BALANCE"
                it[GamePlayLogs.amountCharged] = amount
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
                        gameId = gameId,
                        gameName = game.name,
                        logId = logId,
                        cardId = card.cardId,
                        chargedAmount = amountText,
                        balanceBefore = balanceBeforeText,
                        balanceAfter = balanceAfterText,
                        playedAt = now.toString()
                    )
                )
            )
        } catch (e: Exception) {
            println("Warning: khong tao duoc notification game play: ${e.message}")
        }

        return Result.success(
            UseGameResponse(
                logId = logId,
                gameId = gameId,
                userId = userId,
                cardId = card.cardId,
                ticketId = null,
                remainingTurns = null,
                ticketStatus = null,
                chargedAmount = amount.toString(),
                balanceBefore = balanceBefore.toString(),
                balanceAfter = balanceAfter.toString(),
                balanceTransactionId = txId,
                playedAt = now.toString()
            )
        )
    }
    fun deleteGame(gameId: String): Boolean {
        gameRepository.findById(gameId) ?: return false
        return gameRepository.delete(gameId)
    }

    private fun validateCreateGame(request: CreateGameRequest): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (request.name.isBlank()) {
            errors["name"] = "Tên trò chơi không được để trống"
        }

        try {
            val price = BigDecimal(request.pricePerTurn)
            if (price <= BigDecimal.ZERO) {
                errors["pricePerTurn"] = "Giá vé phải lớn hơn 0"
            }
        } catch (e: NumberFormatException) {
            errors["pricePerTurn"] = "Giá vé không hợp lệ"
        }

        if (request.riskLevel != null && (request.riskLevel < 1 || request.riskLevel > 5)) {
            errors["riskLevel"] = "Risk level phải từ 1 đến 5"
        }

        return errors
    }
}
