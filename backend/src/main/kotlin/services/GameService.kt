package com.park.services

import com.park.database.tables.GamePlayLogs
import com.park.database.tables.Tickets
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
    private val orderRepository: IOrderRepository = OrderRepository(),
    private val cardRepository: ICardRepository = CardRepository()
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
        // Kiểm tra game tồn tại và đang hoạt động
        val game = gameRepository.findById(gameId)
            ?: return Result.failure(NoSuchElementException("Game không tồn tại"))

        if (game.status != "ACTIVE") {
            return Result.failure(IllegalStateException("Game hiện không hoạt động"))
        }

        // Tìm card theo UID (thử physical trước, rồi virtual)
        val card = cardRepository.findByPhysicalUid(request.cardUid)
            ?: cardRepository.findByVirtualUid(request.cardUid)
            ?: return Result.failure(NoSuchElementException("Không tìm thấy thẻ với UID: ${request.cardUid}"))

        if (card.status != "ACTIVE") {
            return Result.failure(IllegalStateException("Thẻ không hoạt động (trạng thái: ${card.status})"))
        }

        val userId = card.userId
            ?: return Result.failure(IllegalStateException("Thẻ chưa được liên kết với tài khoản"))

        // Tìm vé hợp lệ sắp hết hạn nhất
        val ticket = orderRepository.findValidTicketByUserAndGame(userId, gameId)
            ?: return Result.failure(NoSuchElementException("Người dùng không có vé hợp lệ cho game này"))

        val now = Instant.now()
        if (ticket.expiryDate != null && now.isAfter(ticket.expiryDate)) {
            transaction { Tickets.update({ Tickets.ticketId eq ticket.ticketId }) { it[status] = "EXPIRED" } }
            return Result.failure(IllegalStateException("Vé đã hết hạn"))
        }

        // Trừ lượt chơi, nếu về 0 thì đổi status → USED
        val newRemainingTurns = ticket.remainingTurns - 1
        val newStatus = if (newRemainingTurns == 0) "USED" else "VALID"

        transaction {
            Tickets.update({ Tickets.ticketId eq ticket.ticketId }) {
                it[remainingTurns] = newRemainingTurns
                if (newStatus == "USED") {
                    it[status] = "USED"
                    it[usedAt] = now
                }
            }
        }

        // Ghi log chơi game
        val logId = UUID.randomUUID().toString()
        transaction {
            GamePlayLogs.insert {
                it[GamePlayLogs.logId] = logId
                it[GamePlayLogs.userId] = userId
                it[GamePlayLogs.gameId] = gameId
                it[GamePlayLogs.terminalId] = request.terminalId
                it[GamePlayLogs.cardId] = card.cardId
                it[GamePlayLogs.ticketId] = ticket.ticketId
                it[GamePlayLogs.method] = "CARD"
                it[GamePlayLogs.playedAt] = now
            }
        }

        // Cập nhật lastUsedAt của card
        cardRepository.update(card.cardId, mapOf("lastUsedAt" to now))

        // Tăng totalPlays của game
        gameRepository.update(gameId, mapOf("totalPlays" to (game.totalPlays + 1)))

        return Result.success(
            UseGameResponse(
                logId = logId,
                gameId = gameId,
                userId = userId,
                cardId = card.cardId,
                ticketId = ticket.ticketId,
                remainingTurns = newRemainingTurns,
                ticketStatus = newStatus,
                playedAt = now.toString()
            )
        )
    }

    /**
     * Xóa game (Admin only)
     */
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
