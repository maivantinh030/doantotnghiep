package com.park.services

import com.park.dto.*
import com.park.entities.BalanceTransaction
import com.park.entities.Card
import com.park.entities.Game
import com.park.entities.GamePlayLog
import com.park.entities.User
import com.park.repositories.BalanceTransactionRepository
import com.park.repositories.CardRepository
import com.park.repositories.GamePlayLogRepository
import com.park.repositories.GameRepository
import com.park.repositories.IBalanceTransactionRepository
import com.park.repositories.ICardRepository
import com.park.repositories.IGamePlayLogRepository
import com.park.repositories.IGameRepository
import com.park.repositories.IUserRepository
import com.park.repositories.UserRepository
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class GameService(
    private val gameRepository: IGameRepository = GameRepository(),
    private val cardRepository: ICardRepository = CardRepository(),
    private val userRepository: IUserRepository = UserRepository(),
    private val gamePlayLogRepository: IGamePlayLogRepository = GamePlayLogRepository(),
    private val balanceTransactionRepository: IBalanceTransactionRepository = BalanceTransactionRepository(),
    private val notificationService: NotificationService = NotificationService()
) {

    fun getGames(page: Int, size: Int, category: String?, search: String?): PaginatedResponse<GameListItemDTO> {
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

        return PaginatedResponse.build(
            items = games.map { GameListItemDTO.fromEntity(it) },
            total = total,
            page = page,
            size = size
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
            thumbnailUrl = normalizeImageUrl(request.thumbnailUrl),
            galleryUrls = encodeGalleryUrls(request.galleryUrls),
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
            ?: return Result.failure(NoSuchElementException("Game không tồn tại"))

        if (request.status != null && request.status !in listOf("ACTIVE", "MAINTENANCE", "CLOSED")) {
            return Result.failure(IllegalArgumentException("Status phải là ACTIVE, MAINTENANCE hoặc CLOSED"))
        }
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
        request.thumbnailUrl?.let { updates["thumbnailUrl"] = normalizeImageUrl(it) }
        request.galleryUrls?.let { urls -> updates["galleryUrls"] = encodeGalleryUrls(urls) }
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
            ?: return Result.failure(NoSuchElementException("Game không tồn tại"))
        if (game.status != "ACTIVE") {
            return Result.failure(IllegalStateException("Game hiện không hoạt động"))
        }

        val card = try {
            resolveCard(request.cardId, request.cardUid, requireActiveCard = true)
                ?: return Result.failure(IllegalArgumentException("Thiếu mã thẻ để xử lý lượt chơi"))
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val user = try {
            resolveUserForCard(card)
                ?: return Result.failure(IllegalStateException("Thẻ chưa được liên kết với tài khoản"))
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val amount = game.pricePerTurn
        if (amount <= BigDecimal.ZERO) {
            return Result.failure(IllegalStateException("Game chưa có giá hợp lệ"))
        }
        if (user.currentBalance < amount) {
            return Result.failure(IllegalStateException("Số dư không đủ"))
        }

        val now = Instant.now()
        val balanceBefore = user.currentBalance
        val balanceAfter = balanceBefore.subtract(amount)
        val logId = UUID.randomUUID().toString()
        val txId = UUID.randomUUID().toString()

        transaction {
            userRepository.update(user.userId, mapOf(
                "currentBalance" to balanceAfter,
                "updatedAt" to now
            ))
            balanceTransactionRepository.create(
                BalanceTransaction(
                    transactionId = txId,
                    userId = user.userId,
                    amount = amount.negate(),
                    balanceBefore = balanceBefore,
                    balanceAfter = balanceAfter,
                    type = "PAYMENT",
                    referenceType = "GAME_PLAY",
                    referenceId = logId,
                    description = "Chơi trò chơi ${game.name}",
                    createdAt = now,
                    createdBy = null
                )
            )
            gamePlayLogRepository.create(
                GamePlayLog(
                    logId = logId,
                    clientTransactionId = null,
                    userId = user.userId,
                    gameId = gameId,
                    cardId = card.cardId,
                    method = "BALANCE",
                    amountCharged = amount,
                    cardBalanceAfter = balanceAfter,
                    playedAt = now
                )
            )
            cardRepository.update(card.cardId, mapOf(
                "lastUsedAt" to now,
                "updatedAt" to now
            ))
            gameRepository.update(gameId, mapOf(
                "totalPlays" to (game.totalPlays + 1),
                "updatedAt" to now
            ))
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
            return Result.failure(IllegalArgumentException("Mã giao dịch không hợp lệ"))
        }

        findExistingSyncResult(clientTransactionId)?.let { return Result.success(it) }

        val game = gameRepository.findById(gameId)
            ?: return Result.failure(NoSuchElementException("Game không tồn tại"))

        val card = cardRepository.findById(request.cardId.trim())
            ?: return Result.failure(NoSuchElementException("Không tìm thấy thẻ với mã: ${request.cardId}"))

        val user = try {
            resolveUserForCard(card)
                ?: return Result.failure(IllegalStateException("Thẻ chưa được liên kết với tài khoản"))
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val chargedAmount = parsePositiveAmount(request.chargedAmount, "chargedAmount")
            ?: return Result.failure(IllegalArgumentException("Số tiền tính phí không hợp lệ"))
        val cardBalanceAfter = parseNonNegativeAmount(request.cardBalanceAfter, "cardBalanceAfter")
            ?: return Result.failure(IllegalArgumentException("Số dư thẻ sau giao dịch không hợp lệ"))
        val playedAt = runCatching { Instant.parse(request.playedAt) }.getOrElse {
            return Result.failure(IllegalArgumentException("Thời gian chơi không hợp lệ"))
        }

        val balanceBefore = cardBalanceAfter.add(chargedAmount)
        val logId = UUID.randomUUID().toString()
        val txId = UUID.randomUUID().toString()
        val syncNow = Instant.now()

        transaction {
            userRepository.update(user.userId, mapOf(
                "currentBalance" to cardBalanceAfter,
                "updatedAt" to syncNow
            ))
            balanceTransactionRepository.create(
                BalanceTransaction(
                    transactionId = txId,
                    userId = user.userId,
                    amount = chargedAmount.negate(),
                    balanceBefore = balanceBefore,
                    balanceAfter = cardBalanceAfter,
                    type = "PAYMENT",
                    referenceType = "GAME_PLAY",
                    referenceId = logId,
                    description = "Chơi trò chơi ${game.name}",
                    createdAt = playedAt,
                    createdBy = null
                )
            )
            gamePlayLogRepository.create(
                GamePlayLog(
                    logId = logId,
                    clientTransactionId = clientTransactionId,
                    userId = user.userId,
                    gameId = gameId,
                    cardId = card.cardId,
                    method = "CARD",
                    amountCharged = chargedAmount,
                    cardBalanceAfter = cardBalanceAfter,
                    playedAt = playedAt
                )
            )
            cardRepository.update(card.cardId, mapOf(
                "lastUsedAt" to playedAt,
                "updatedAt" to syncNow
            ))
            gameRepository.update(gameId, mapOf(
                "totalPlays" to (game.totalPlays + 1),
                "updatedAt" to syncNow
            ))
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

    fun getUserStats(userId: String): UserStatsDTO {
        val joinDate = userRepository.findById(userId)?.createdAt?.toString()
            ?: Instant.now().toString()

        val gameIds = gamePlayLogRepository.findGameIdsByUser(userId)
        val totalVisits = gameIds.size

        val favoriteGameId = gameIds
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        val favoriteGameName = favoriteGameId?.let { gameRepository.findById(it)?.name }

        return UserStatsDTO(
            joinDate = joinDate,
            totalVisits = totalVisits,
            favoriteGame = favoriteGameName
        )
    }

    fun getMyGamePlayHistory(userId: String, page: Int, size: Int): GamePlayHistoryPageDTO {
        val safePage = if (page < 1) 1 else page
        val safeSize = if (size < 1) 10 else size
        val offset = ((safePage - 1) * safeSize).toLong()

        val allLogs = gamePlayLogRepository.findAllByUser(userId)
        val total = allLogs.size.toLong()
        val totalAmount = allLogs.fold(BigDecimal.ZERO) { acc, log -> acc.add(log.amountCharged) }
        val uniqueGames = allLogs.map { it.gameId }.distinct().size

        val items = gamePlayLogRepository.findHistoryByUser(userId, safeSize, offset)

        return GamePlayHistoryPageDTO(
            items = items,
            total = total,
            page = safePage,
            size = safeSize,
            totalPages = if (safeSize == 0) 0L else ((total + safeSize - 1) / safeSize),
            totalAmount = totalAmount.toPlainString(),
            uniqueGames = uniqueGames
        )
    }

    private fun resolveCard(cardId: String?, cardUid: String?, requireActiveCard: Boolean): Card? {
        val normalizedCardId = cardId?.trim().takeUnless { it.isNullOrBlank() }
        val normalizedCardUid = cardUid?.trim().takeUnless { it.isNullOrBlank() }

        val card = when {
            normalizedCardId != null -> cardRepository.findById(normalizedCardId)
                ?: throw NoSuchElementException("Không tìm thấy thẻ với mã: $normalizedCardId")
            normalizedCardUid != null -> cardRepository.findByPhysicalUid(normalizedCardUid)
                ?: throw NoSuchElementException("Không tìm thấy thẻ với UID: $normalizedCardUid")
            else -> return null
        }

        if (requireActiveCard && card.status != "ACTIVE") {
            throw IllegalStateException("Thẻ không hoạt động (trạng thái: ${card.status})")
        }
        return card
    }

    private fun resolveUserForCard(card: Card): User? {
        val userId = card.userId ?: return null
        return userRepository.findById(userId)
            ?: throw NoSuchElementException("Không tìm thấy người dùng liên kết với thẻ")
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

    private fun normalizeImageUrl(url: String?): String? {
        val trimmed = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val uploadsIndex = trimmed.indexOf("/uploads/")
        return if (uploadsIndex > 0) trimmed.substring(uploadsIndex) else trimmed
    }

    private fun encodeGalleryUrls(urls: List<String>?): String? {
        val normalizedUrls = urls
            ?.mapNotNull { normalizeImageUrl(it) }
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return "[${normalizedUrls.joinToString(",") { "\"${it.replace("\"", "\\\"")}\"" }}]"
    }

    private fun findExistingSyncResult(clientTransactionId: String): UseGameResponse? {
        val log = gamePlayLogRepository.findByClientTransactionId(clientTransactionId)
            ?: return null

        val tx = balanceTransactionRepository.findLatestByReference("GAME_PLAY", log.logId)

        return UseGameResponse(
            logId = log.logId,
            gameId = log.gameId,
            userId = log.userId,
            cardId = log.cardId.orEmpty(),
            clientTransactionId = log.clientTransactionId,
            chargedAmount = log.amountCharged.toString(),
            balanceBefore = tx?.balanceBefore?.toString(),
            balanceAfter = tx?.balanceAfter?.toString() ?: log.cardBalanceAfter?.toString(),
            cardBalanceAfter = log.cardBalanceAfter?.toString(),
            balanceTransactionId = tx?.transactionId,
            playedAt = log.playedAt.toString()
        )
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
                title = "Bạn vừa chơi ${game.name}",
                message = "Đã trừ $amountText VND cho lượt chơi ${game.name}. Số dư còn lại: $balanceAfterText VND.",
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
            errors["name"] = "Tên trò chơi không được để trống"
        }

        try {
            val price = BigDecimal(request.pricePerTurn)
            if (price <= BigDecimal.ZERO) {
                errors["pricePerTurn"] = "Gia ve phai lon hon 0"
            }
        } catch (_: NumberFormatException) {
            errors["pricePerTurn"] = "Giá vé không hợp lệ"
        }

        if (request.riskLevel != null && (request.riskLevel < 1 || request.riskLevel > 5)) {
            errors["riskLevel"] = "Risk level phai tu 1 den 5"
        }

        return errors
    }
}
