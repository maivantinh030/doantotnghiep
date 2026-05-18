package com.park.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.park.database.tables.*
import com.park.dto.*
import com.park.entities.Admin
import com.park.entities.BalanceTransaction
import com.park.entities.SupportMessage
import com.park.repositories.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import java.time.ZoneId
import java.util.*

class AdminService(
    private val accountRepository: IAccountRepository = AccountRepository(),
    private val adminRepository: IAdminRepository = AdminRepository(),
    private val supportRepository: ISupportRepository = SupportRepository(),
    private val balanceTransactionRepository: IBalanceTransactionRepository = BalanceTransactionRepository(),
    private val gameRepository: IGameRepository = GameRepository(),
    private val notificationService: NotificationService = NotificationService(),
    private val statisticsRepository: IStatisticsRepository = StatisticsRepository()
) {

    companion object {
        private const val JWT_SECRET = "park-adventure-secret-key-2024"
        private const val JWT_ISSUER = "park-adventure"
        private const val JWT_AUDIENCE = "park-adventure-users"
        private const val JWT_VALIDITY_MS = 86400000L * 30 // 30 days
    }

    // ─── Auth ─────────────────────────────────────────────────────────────

    fun registerAdmin(request: CreateAdminRequest): AdminAuthResponse {
        return try {
            if (accountRepository.existsByPhoneNumber(request.phoneNumber)) {
                return AdminAuthResponse(success = false, message = "Số điện thoại đã được sử dụng")
            }
            val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
            val account = accountRepository.create(
                CreateAccountDTO(
                    phoneNumber = request.phoneNumber,
                    passwordHash = hashedPassword,
                    role = "ADMIN"
                )
            )
            val adminId = UUID.randomUUID().toString()
            val admin = adminRepository.create(
                adminId = adminId,
                accountId = account.accountId,
                fullName = request.fullName,
                employeeCode = request.employeeCode
            )
            val token = generateToken(account.accountId, adminId, "ADMIN")
            AdminAuthResponse(
                success = true,
                message = "Tạo tài khoản admin thành công",
                data = AdminAuthData(
                    token = token,
                    admin = AdminInfo.fromEntity(admin, account.phoneNumber)
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AdminAuthResponse(success = false, message = "Lỗi hệ thống: ${e.message}")
        }
    }

    fun loginAdmin(request: AdminLoginRequest): AdminAuthResponse {
        return try {
            val account = accountRepository.findByPhoneNumber(request.phoneNumber)
                ?: return AdminAuthResponse(success = false, message = "Số điện thoại hoặc mật khẩu không đúng")

            if (account.role != "ADMIN") {
                return AdminAuthResponse(success = false, message = "Tài khoản không có quyền admin")
            }
            if (account.status == "BANNED") {
                return AdminAuthResponse(success = false, message = "Tài khoản đã bị khóa")
            }
            if (!BCrypt.checkpw(request.password, account.passwordHash)) {
                return AdminAuthResponse(success = false, message = "Số điện thoại hoặc mật khẩu không đúng")
            }

            accountRepository.updateLastLogin(account.accountId)

            val admin = adminRepository.findByAccountId(account.accountId)
                ?: return AdminAuthResponse(success = false, message = "Không tìm thấy hồ sơ admin")

            adminRepository.updateLastAction(admin.adminId)
            val token = generateToken(account.accountId, admin.adminId, "ADMIN")
            AdminAuthResponse(
                success = true,
                message = "Đăng nhập thành công",
                data = AdminAuthData(
                    token = token,
                    admin = AdminInfo.fromEntity(admin, account.phoneNumber)
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AdminAuthResponse(success = false, message = "Lỗi hệ thống: ${e.message}")
        }
    }

    // ─── Dashboard ────────────────────────────────────────────────────────

    fun getDashboardStats(): Map<String, Any> {
        return transaction {
            val totalUsers = Users.selectAll().count()
            val activeGames = Games.selectAll().where { Games.status eq "ACTIVE" }.count()
            val activeCards = Cards.selectAll().where { Cards.status eq "ACTIVE" }.count()
            val availableCards = Cards.selectAll().where { Cards.status eq "AVAILABLE" }.count()
            val blockedCards = Cards.selectAll().where { Cards.status eq "BLOCKED" }.count()
            val totalTopUpRevenue = PaymentRecords.selectAll()
                .where { PaymentRecords.status eq "SUCCESS" }
                .sumOf { it[PaymentRecords.amount] }

            mapOf(
                "totalUsers" to totalUsers,
                "totalGames" to activeGames,
                "activeCards" to activeCards,
                "availableCards" to availableCards,
                "blockedCards" to blockedCards,
                "totalTopUpRevenue" to totalTopUpRevenue.toDouble()
            )
        }
    }

    fun getRevenueChart(period: String): RevenueChartResponse {
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val today = LocalDate.now(zone)
        return when (period) {
            "weekly" -> {
                val weeks = (7 downTo 0).map { today.minusWeeks(it.toLong()) }
                val startInstant = weeks.first().atStartOfDay(zone).toInstant()
                val rows = transaction {
                    PaymentRecords.selectAll()
                        .where {
                            (PaymentRecords.status eq "SUCCESS") and
                            (PaymentRecords.createdAt greaterEq startInstant)
                        }
                        .map { row ->
                            val date = row[PaymentRecords.createdAt].atZone(zone).toLocalDate()
                            date to row[PaymentRecords.amount]
                        }
                }
                val labels = weeks.map { w -> "${w.dayOfMonth}/${w.monthValue}" }
                val values = weeks.map { weekStart ->
                    val weekEnd = weekStart.plusWeeks(1)
                    rows.filter { (d, _) -> !d.isBefore(weekStart) && d.isBefore(weekEnd) }
                        .fold(BigDecimal.ZERO) { acc, (_, v) -> acc + v }.toDouble()
                }
                RevenueChartResponse(labels = labels, values = values, totalInPeriod = values.sum())
            }
            "monthly" -> {
                val months = (11 downTo 0).map { today.withDayOfMonth(1).minusMonths(it.toLong()) }
                val startInstant = months.first().atStartOfDay(zone).toInstant()
                val rows = transaction {
                    PaymentRecords.selectAll()
                        .where {
                            (PaymentRecords.status eq "SUCCESS") and
                            (PaymentRecords.createdAt greaterEq startInstant)
                        }
                        .map { row ->
                            val date = row[PaymentRecords.createdAt].atZone(zone).toLocalDate()
                            date to row[PaymentRecords.amount]
                        }
                }
                val labels = months.map { m -> "Th${m.monthValue}" }
                val values = months.map { monthStart ->
                    val monthEnd = monthStart.plusMonths(1)
                    rows.filter { (d, _) -> !d.isBefore(monthStart) && d.isBefore(monthEnd) }
                        .fold(BigDecimal.ZERO) { acc, (_, v) -> acc + v }.toDouble()
                }
                RevenueChartResponse(labels = labels, values = values, totalInPeriod = values.sum())
            }
            else -> { // daily
                val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
                val startInstant = days.first().atStartOfDay(zone).toInstant()
                val rows = transaction {
                    PaymentRecords.selectAll()
                        .where {
                            (PaymentRecords.status eq "SUCCESS") and
                            (PaymentRecords.createdAt greaterEq startInstant)
                        }
                        .map { row ->
                            val date = row[PaymentRecords.createdAt].atZone(zone).toLocalDate()
                            date to row[PaymentRecords.amount]
                        }
                }
                val grouped = rows.groupBy({ it.first }) { it.second }
                val labels = days.map { d -> "${d.dayOfMonth}/${d.monthValue}" }
                val values = days.map { day ->
                    grouped[day]?.fold(BigDecimal.ZERO) { acc, v -> acc + v }?.toDouble() ?: 0.0
                }
                RevenueChartResponse(labels = labels, values = values, totalInPeriod = values.sum())
            }
        }
    }

    // ─── User Management ─────────────────────────────────────────────────

    fun getStatisticsFilters(): AdminStatisticsFiltersDTO {
        return transaction {
            val games = Games.selectAll()
                .orderBy(Games.name, SortOrder.ASC)
                .map {
                    AdminStatisticsFilterOptionDTO(
                        gameId = it[Games.gameId],
                        name = it[Games.name],
                        area = it[Games.location]
                    )
                }

            val areas = Games.select(Games.location)
                .where { Games.location.isNotNull() }
                .withDistinct()
                .mapNotNull { it[Games.location] }
                .sorted()

            AdminStatisticsFiltersDTO(
                games = games,
                areas = areas,
                ticketTypes = listOf("ALL", "STANDARD", "PRIORITY", "FAMILY"),
                statuses = listOf("ALL", "ACTIVE", "MAINTENANCE", "CLOSED"),
                groupings = listOf("daily", "weekly", "monthly")
            )
        }
    }

    fun getStatisticsTrend(
        period: String,
        startDate: String?,
        endDate: String?,
        game: String?,
        area: String?,
        status: String?
    ): AdminStatisticsTrendDTO {
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val normalizedPeriod = normalizePeriod(period)
        val parsedStart = parseDateOrThrow(startDate, "startDate")
        val parsedEnd = parseDateOrThrow(endDate, "endDate")
        val (rangeStart, rangeEnd) = resolveDateRange(normalizedPeriod, parsedStart, parsedEnd, zone)
        val startInstant = rangeStart.atStartOfDay(zone).toInstant()
        val endExclusive = rangeEnd.plusDays(1).atStartOfDay(zone).toInstant()

        val games = statisticsRepository.loadGameMetas()
            .filter { matchOptionalFilter(it.name, it.gameId, game) }
            .filter { matchOptionalValue(it.area, area) }
            .filter { matchOptionalValue(it.status, status) }

        val buckets = buildTrendBuckets(normalizedPeriod, rangeStart, rangeEnd)
        if (games.isEmpty()) {
            return AdminStatisticsTrendDTO(
                labels = buckets.map { it.label },
                revenueValues = buckets.map { 0.0 },
                playerValues = buckets.map { 0 },
                playValues = buckets.map { 0 },
                newUserValues = buckets.map { 0 },
                totalRevenue = 0.0,
                totalPlayers = 0,
                totalPlays = 0,
                newUserCount = 0
            )
        }

        val gameIds = games.map { it.gameId }.toSet()
        val logs = statisticsRepository.queryPlayLogs(startInstant, endExclusive, gameIds)

        val keySelector: (Instant) -> LocalDate = when (normalizedPeriod) {
            "weekly" -> { instant ->
                instant.atZone(zone).toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            }

            "monthly" -> { instant ->
                instant.atZone(zone).toLocalDate().withDayOfMonth(1)
            }

            else -> { instant ->
                instant.atZone(zone).toLocalDate()
            }
        }

        val grouped = logs.groupBy { keySelector(it.playedAt) }
        val revenueValues = buckets.map { bucket ->
            grouped[bucket.bucketStart]
                ?.fold(BigDecimal.ZERO) { acc, row -> acc + row.amountCharged }
                ?.toDouble()
                ?: 0.0
        }
        val playerValues = buckets.map { bucket ->
            grouped[bucket.bucketStart]
                ?.map { it.userId }
                ?.distinct()
                ?.size
                ?: 0
        }
        val playValues = buckets.map { bucket -> grouped[bucket.bucketStart]?.size ?: 0 }

        // newUserValues: cùng key bucket nhưng tính cho Users.createdAt.
        val newUsersByBucket = statisticsRepository.newUserCountsByBucket(
            startInstant, endExclusive
        ) { ins -> keySelector(ins).atStartOfDay(zone).toInstant() }
        val newUserValues = buckets.map { bucket ->
            val key = bucket.bucketStart.atStartOfDay(zone).toInstant()
            newUsersByBucket[key] ?: 0
        }

        return AdminStatisticsTrendDTO(
            labels = buckets.map { it.label },
            revenueValues = revenueValues,
            playerValues = playerValues,
            playValues = playValues,
            newUserValues = newUserValues,
            totalRevenue = revenueValues.sum(),
            totalPlayers = logs.map { it.userId }.distinct().size,
            totalPlays = logs.size,
            newUserCount = newUserValues.sum()
        )
    }

    fun getStatisticsGames(
        startDate: String?,
        endDate: String?,
        game: String?,
        area: String?,
        status: String?,
        search: String?
    ): AdminStatisticsGamesResponseDTO {
        val (items, summary) = collectStatisticsItems(
            startDate = startDate,
            endDate = endDate,
            game = game,
            area = area,
            status = status,
            search = search
        )

        val topRevenue = items.sortedByDescending { it.revenue }.take(5).map {
            AdminStatisticsTopItemDTO(
                gameId = it.gameId,
                name = it.name,
                players = it.players,
                revenue = it.revenue
            )
        }

        val lowPlayers = items.sortedBy { it.players }.take(5).map {
            AdminStatisticsTopItemDTO(
                gameId = it.gameId,
                name = it.name,
                players = it.players,
                revenue = it.revenue
            )
        }

        val cardStatus = transaction {
            AdminStatisticsCardStatusDTO(
                active = Cards.selectAll().where { Cards.status eq "ACTIVE" }.count().toInt(),
                available = Cards.selectAll().where { Cards.status eq "AVAILABLE" }.count().toInt(),
                blocked = Cards.selectAll().where { Cards.status eq "BLOCKED" }.count().toInt()
            )
        }

        return AdminStatisticsGamesResponseDTO(
            items = items,
            summary = summary,
            topRevenue = topRevenue,
            lowPlayers = lowPlayers,
            cardStatus = cardStatus
        )
    }

    fun getStatisticsTable(
        page: Int,
        size: Int,
        startDate: String?,
        endDate: String?,
        game: String?,
        area: String?,
        status: String?,
        search: String?
    ): AdminStatisticsTableResponseDTO {
        val safePage = if (page <= 0) 1 else page
        val safeSize = size.coerceIn(1, 200)
        val (items, summary) = collectStatisticsItems(
            startDate = startDate,
            endDate = endDate,
            game = game,
            area = area,
            status = status,
            search = search
        )

        val total = items.size.toLong()
        val totalPages = if (total == 0L) 0L else (total + safeSize - 1) / safeSize
        val offset = ((safePage - 1) * safeSize).coerceAtLeast(0)
        val paged = items.drop(offset).take(safeSize)

        return AdminStatisticsTableResponseDTO(
            items = paged,
            total = total,
            page = safePage,
            size = safeSize,
            totalPages = totalPages,
            summary = summary
        )
    }

    // ─── Dashboard mở rộng ───────────────────────────────────────────────

    fun getHourlyTrend(
        startDate: String?,
        endDate: String?,
        game: String?,
        area: String?,
        status: String?
    ): AdminHourlyTrendDTO {
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val parsedStart = parseDateOrThrow(startDate, "startDate")
        val parsedEnd = parseDateOrThrow(endDate, "endDate")
        val (rangeStart, rangeEnd) = resolveDateRange("daily", parsedStart, parsedEnd, zone)
        val startInstant = rangeStart.atStartOfDay(zone).toInstant()
        val endExclusive = rangeEnd.plusDays(1).atStartOfDay(zone).toInstant()

        val filteredIds = statisticsRepository.loadGameMetas()
            .filter { matchOptionalFilter(it.name, it.gameId, game) }
            .filter { matchOptionalValue(it.area, area) }
            .filter { matchOptionalValue(it.status, status) }
            .map { it.gameId }
            .toSet()

        val rows = statisticsRepository.queryPlayLogs(startInstant, endExclusive, filteredIds)
        val bucketHours = (6..21).toList()   // 16 buckets
        val grouped = rows.groupBy { it.playedAt.atZone(zone).hour }

        val plays = bucketHours.map { h -> grouped[h]?.size ?: 0 }
        val players = bucketHours.map { h ->
            grouped[h]?.map { it.userId }?.distinct()?.size ?: 0
        }
        val revenue = bucketHours.map { h ->
            grouped[h]?.fold(BigDecimal.ZERO) { acc, r -> acc + r.amountCharged }?.toDouble() ?: 0.0
        }
        val peakIdx = plays.withIndex().maxByOrNull { it.value }?.takeIf { it.value > 0 }?.index
        return AdminHourlyTrendDTO(
            labels = bucketHours.map { "${it}h" },
            playValues = plays,
            playerValues = players,
            revenueValues = revenue,
            peakHourLabel = peakIdx?.let { "${bucketHours[it]}h" }
        )
    }

    fun getDowTrend(
        startDate: String?,
        endDate: String?,
        gameId: String?
    ): AdminDowTrendDTO {
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val parsedStart = parseDateOrThrow(startDate, "startDate")
        val parsedEnd = parseDateOrThrow(endDate, "endDate")
        val (rangeStart, rangeEnd) = resolveDateRange("daily", parsedStart, parsedEnd, zone)
        val startInstant = rangeStart.atStartOfDay(zone).toInstant()
        val endExclusive = rangeEnd.plusDays(1).atStartOfDay(zone).toInstant()

        val rows = if (gameId.isNullOrBlank()) {
            statisticsRepository.queryPlayLogs(startInstant, endExclusive, null)
        } else {
            statisticsRepository.queryPlayLogsForGame(gameId, startInstant, endExclusive)
        }

        val plays = IntArray(7)
        val revenue = DoubleArray(7)
        val users = Array(7) { mutableSetOf<String>() }
        rows.forEach { r ->
            val dow = r.playedAt.atZone(zone).dayOfWeek.value - 1   // 0..6
            plays[dow] += 1
            revenue[dow] += r.amountCharged.toDouble()
            users[dow].add(r.userId)
        }
        return AdminDowTrendDTO(
            labels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN"),
            playValues = plays.toList(),
            playerValues = users.map { it.size },
            revenueValues = revenue.toList()
        )
    }

    fun getHeatmap(weeks: Int = 4): AdminHeatmapDTO {
        require(weeks in 1..12) { "weeks phải từ 1 đến 12" }
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val today = LocalDate.now(zone)
        val mondayThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val anchor = mondayThisWeek.minusWeeks((weeks - 1).toLong())
        val startInstant = anchor.atStartOfDay(zone).toInstant()
        val endExclusive = today.plusDays(1).atStartOfDay(zone).toInstant()

        val rows = statisticsRepository.queryPlayLogs(startInstant, endExclusive, null)
        val matrix = Array(weeks) { IntArray(7) }
        rows.forEach { r ->
            val d = r.playedAt.atZone(zone).toLocalDate()
            val diff = ChronoUnit.DAYS.between(anchor, d).toInt()
            val wi = diff / 7
            val di = diff % 7
            if (wi in 0 until weeks && di in 0..6) matrix[wi][di] += 1
        }
        val maxVal = matrix.maxOfOrNull { it.maxOrNull() ?: 0 } ?: 0
        return AdminHeatmapDTO(matrix.map { it.toList() }, maxVal)
    }

    fun getCardChannel(startDate: String?, endDate: String?): AdminCardChannelDTO {
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val parsedStart = parseDateOrThrow(startDate, "startDate")
        val parsedEnd = parseDateOrThrow(endDate, "endDate")
        val (rangeStart, rangeEnd) = resolveDateRange("daily", parsedStart, parsedEnd, zone)
        val startInstant = rangeStart.atStartOfDay(zone).toInstant()
        val endExclusive = rangeEnd.plusDays(1).atStartOfDay(zone).toInstant()
        val (app, counter) = statisticsRepository.cardChannelCounts(startInstant, endExclusive)
        return AdminCardChannelDTO(app, counter)
    }

    fun getCardLifecycle(startDate: String?, endDate: String?): AdminCardLifecycleDTO {
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val today = LocalDate.now(zone)
        val parsedStart = parseDateOrThrow(startDate, "startDate")
        val parsedEnd = parseDateOrThrow(endDate, "endDate")
        val rangeStart = parsedStart ?: today.withDayOfMonth(1)
        val rangeEnd = parsedEnd ?: today
        require(!rangeEnd.isBefore(rangeStart)) { "endDate must be greater than or equal to startDate" }

        val startInstant = rangeStart.atStartOfDay(zone).toInstant()
        val endExclusive = rangeEnd.plusDays(1).atStartOfDay(zone).toInstant()
        val counts = statisticsRepository.cardLifecycleCounts(startInstant, endExclusive)
        return AdminCardLifecycleDTO(
            issuedThisMonth = counts.issued,
            blockedThisMonth = counts.blocked,
            pendingRequests = counts.pendingRequests
        )
    }

    fun getGameDetail(gameId: String, days: Int = 90): AdminGameDetailDTO {
        require(days in 1..365) { "days phải từ 1 đến 365" }
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val today = LocalDate.now(zone)
        val game = statisticsRepository.findGame(gameId)
            ?: throw IllegalArgumentException("Không tìm thấy trò chơi: $gameId")

        // Query 1: daily (last `days` ngày)
        val dayAnchor = today.minusDays((days - 1).toLong())
        val dailyStartInstant = dayAnchor.atStartOfDay(zone).toInstant()
        val endExclusive = today.plusDays(1).atStartOfDay(zone).toInstant()
        val dailyRows = statisticsRepository.queryPlayLogsForGame(gameId, dailyStartInstant, endExclusive)

        // Query 2: monthly (12 tháng gần nhất từ đầu tháng -11 đến hôm nay)
        val monthAnchor = today.minusMonths(11).withDayOfMonth(1)
        val monthlyStartInstant = monthAnchor.atStartOfDay(zone).toInstant()
        val monthlyRows = if (monthlyStartInstant < dailyStartInstant) {
            statisticsRepository.queryPlayLogsForGame(gameId, monthlyStartInstant, endExclusive)
        } else {
            dailyRows   // daily đủ bao quát monthly window
        }

        // Bucket daily
        val dRev = DoubleArray(days)
        val dPlays = IntArray(days)
        dailyRows.forEach { r ->
            val d = r.playedAt.atZone(zone).toLocalDate()
            val idx = ChronoUnit.DAYS.between(dayAnchor, d).toInt()
            if (idx in 0 until days) {
                dRev[idx] += r.amountCharged.toDouble()
                dPlays[idx] += 1
            }
        }

        // Bucket monthly 12
        val mRev = DoubleArray(12)
        val mPlays = IntArray(12)
        val monthLabels = (0..11).map {
            val m = monthAnchor.plusMonths(it.toLong())
            "T${m.monthValue}/${m.year}"
        }
        monthlyRows.forEach { r ->
            val d = r.playedAt.atZone(zone).toLocalDate().withDayOfMonth(1)
            val diff = (d.year - monthAnchor.year) * 12 + (d.monthValue - monthAnchor.monthValue)
            if (diff in 0..11) {
                mRev[diff] += r.amountCharged.toDouble()
                mPlays[diff] += 1
            }
        }

        // DOW (toàn dailyRows)
        val dowPlays = IntArray(7)
        dailyRows.forEach { dowPlays[it.playedAt.atZone(zone).dayOfWeek.value - 1] += 1 }

        // Hour (6..21)
        val hourPlays = IntArray(16)
        dailyRows.forEach {
            val h = it.playedAt.atZone(zone).hour
            if (h in 6..21) hourPlays[h - 6] += 1
        }

        // Peak hour = window 3 giờ liên tiếp tổng max
        val peakStart = (0..13).maxByOrNull {
            hourPlays[it] + hourPlays[it + 1] + hourPlays[it + 2]
        } ?: 0
        val peakLabel = "${peakStart + 6}h–${peakStart + 8}h"

        // Return rate = % user chơi ≥ 2 lần
        val byUser = dailyRows.groupingBy { it.userId }.eachCount()
        val returnRate = if (byUser.isEmpty()) 0.0
        else byUser.count { it.value >= 2 }.toDouble() / byUser.size * 100.0

        return AdminGameDetailDTO(
            gameId = gameId,
            name = game.name,
            category = game.category ?: "Khác",
            ticketPrice = game.ticketPrice.toDouble(),
            durationMinutes = game.durationMinutes,
            peakHour = peakLabel,
            returnRate = returnRate,
            avgSessionMin = game.durationMinutes?.toDouble() ?: 5.0,
            revenueDaily = dRev.toList(),
            playsDaily = dPlays.toList(),
            revenueByMonth = mRev.toList(),
            playsByMonth = mPlays.toList(),
            monthLabels = monthLabels,
            playsByDow = dowPlays.toList(),
            playsByHour = hourPlays.toList()
        )
    }

    fun getAllUsers(page: Int, size: Int): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()
        return transaction {
            val query = Users.join(Accounts, JoinType.INNER, Users.accountId, Accounts.accountId)
            val total = query.selectAll().count()
            val users = query.selectAll()
                .orderBy(Users.createdAt, SortOrder.DESC)
                .limit(size).offset(offset)
                .map { row ->
                    AdminUserDTO(
                        userId = row[Users.userId],
                        accountId = row[Users.accountId] ?: "",
                        phoneNumber = row[Accounts.phoneNumber],
                        fullName = row[Users.fullName],
                        email = row[Users.email],
                        currentBalance = row[Users.currentBalance].toString(),
                        accountStatus = row[Accounts.status],
                        createdAt = row[Users.createdAt].toString()
                    )
                }
            mapOf("items" to users, "total" to total, "page" to page, "size" to size)
        }
    }

    fun lockUser(userId: String): Boolean {
        return transaction {
            val user = Users.selectAll().where { Users.userId eq userId }.singleOrNull()
                ?: return@transaction false
            val accountId = user[Users.accountId] ?: return@transaction false
            Accounts.update(where = { Accounts.accountId eq accountId }) {
                it[status] = "BANNED"
                it[updatedAt] = Instant.now()
            } > 0
        }
    }

    fun unlockUser(userId: String): Boolean {
        return transaction {
            val user = Users.selectAll().where { Users.userId eq userId }.singleOrNull()
                ?: return@transaction false
            val accountId = user[Users.accountId] ?: return@transaction false
            Accounts.update(where = { Accounts.accountId eq accountId }) {
                it[status] = "ACTIVE"
                it[updatedAt] = Instant.now()
            } > 0
        }
    }

    fun adjustBalance(userId: String, request: AdjustBalanceRequest, adminId: String): Result<Map<String, Any>> {
        return try {
            val result = transaction {
                val userRow = Users.selectAll().where { Users.userId eq userId }.singleOrNull()
                    ?: return@transaction null

                val currentBalance = userRow[Users.currentBalance]
                val adjustAmount = BigDecimal(request.amount.toString())
                val newBalance = currentBalance.add(adjustAmount)

                if (newBalance < BigDecimal.ZERO) {
                    return@transaction null
                }

                Users.update(where = { Users.userId eq userId }) {
                    it[Users.currentBalance] = newBalance
                    it[Users.updatedAt] = Instant.now()
                }

                balanceTransactionRepository.create(
                    BalanceTransaction(
                        transactionId = UUID.randomUUID().toString(),
                        userId = userId,
                        amount = adjustAmount,
                        balanceBefore = currentBalance,
                        balanceAfter = newBalance,
                        type = "ADJUSTMENT",
                        referenceType = "ADMIN",
                        referenceId = adminId,
                        description = request.description ?: "Admin điều chỉnh số dư",
                        createdAt = Instant.now(),
                        createdBy = adminId
                    )
                )

                mapOf(
                    "userId" to userId,
                    "previousBalance" to currentBalance.toString(),
                    "newBalance" to newBalance.toString(),
                    "adjustAmount" to adjustAmount.toString()
                )
            }
            if (result != null) {
                val adjustAmount = BigDecimal(request.amount.toString())
                if (adjustAmount > BigDecimal.ZERO) {
                    val newBalanceText = result["newBalance"] as String
                    notificationService.createNotification(
                        userId = userId,
                        type = "TOPUP",
                        title = "Được cộng tiền vào ví",
                        message = "Quản lý vừa cộng ${adjustAmount.stripTrailingZeros().toPlainString()} VND vào ví của bạn. Số dư hiện tại: $newBalanceText VND.",
                        data = null
                    )
                }
                Result.success(result)
            } else Result.failure(IllegalArgumentException("Số dư không đủ hoặc người dùng không tồn tại"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Transactions ─────────────────────────────────────────────────────

    fun getAllTransactions(page: Int, size: Int): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()
        return transaction {
            val query = BalanceTransactions.join(Users, JoinType.LEFT, BalanceTransactions.userId, Users.userId)
            val total = BalanceTransactions.selectAll().count()
            val txs = query.selectAll()
                .orderBy(BalanceTransactions.createdAt, SortOrder.DESC)
                .limit(size).offset(offset)
                .map { row ->
                    AdminTransactionDTO(
                        transactionId = row[BalanceTransactions.transactionId],
                        userId = row[BalanceTransactions.userId],
                        amount = row[BalanceTransactions.amount].toString(),
                        balanceBefore = row[BalanceTransactions.balanceBefore].toString(),
                        balanceAfter = row[BalanceTransactions.balanceAfter].toString(),
                        type = row[BalanceTransactions.type],
                        referenceType = row[BalanceTransactions.referenceType],
                        referenceId = row[BalanceTransactions.referenceId],
                        description = row[BalanceTransactions.description],
                        createdAt = row[BalanceTransactions.createdAt].toString(),
                        createdBy = row[BalanceTransactions.createdBy]
                    )
                }
            mapOf("items" to txs, "total" to total, "page" to page, "size" to size)
        }
    }

    // ─── Notifications ────────────────────────────────────────────────────

    fun sendBroadcastNotification(adminId: String, request: SendNotificationRequest): Result<Map<String, Any>> {
        return try {
            val targetUserIds = transaction {
                when (request.targetType) {
                    "USER" -> if (request.targetUserId != null) listOf(request.targetUserId) else emptyList()
                    else -> Users.selectAll().map { it[Users.userId] } // ALL
                }
            }

            if (targetUserIds.isEmpty()) {
                return Result.failure(IllegalArgumentException("Không tìm thấy người dùng phù hợp"))
            }

            val now = Instant.now()
            val broadcastId = UUID.randomUUID().toString()
            val payload = NotificationDataCodec.encode(
                BroadcastNotificationData(
                    broadcastId = broadcastId,
                    targetType = request.targetType,
                    sentBy = adminId
                )
            )

            targetUserIds.forEach { uid ->
                notificationService.createNotification(
                    userId = uid,
                    type = "SYSTEM",
                    title = request.title,
                    message = request.message,
                    data = payload
                )
            }

            Result.success(mapOf(
                "broadcastId" to broadcastId,
                "sentCount" to targetUserIds.size,
                "targetType" to request.targetType,
                "sentAt" to now.toString()
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBroadcastHistory(page: Int, size: Int): Map<String, Any> {
        return transaction {
            val allNotifications = Notifications.selectAll()
                .where {
                    (Notifications.type eq "SYSTEM") and
                    (Notifications.data.isNotNull())
                }
                .orderBy(Notifications.createdAt, SortOrder.DESC)
                .limit(size * 10)
                .map { row ->
                    Triple(
                        row[Notifications.notificationId],
                        row[Notifications.title],
                        row[Notifications.message]
                    ) to Pair(
                        row[Notifications.data],
                        row[Notifications.createdAt]
                    )
                }

            val allBroadcasts = allNotifications
                .mapNotNull { (notification, dataAndTime) ->
                    val data = dataAndTime.first
                    val createdAt = dataAndTime.second
                    val payload = NotificationDataCodec.decodeBroadcast(data)
                    val broadcastId = payload?.broadcastId
                        ?: """"broadcastId":"([^"]+)"""".toRegex().find(data ?: "")?.groupValues?.get(1)
                    val targetType = payload?.targetType
                        ?: """"targetType":"([^"]+)"""".toRegex().find(data ?: "")?.groupValues?.get(1)
                    broadcastId?.let {
                        it to AdminSentNotificationDTO(
                            notificationId = it,
                            title = notification.second,
                            message = notification.third,
                            targetType = targetType ?: "ALL",
                            createdAt = createdAt.toString()
                        )
                    }
                }
                .distinctBy { it.first }
                .map { it.second }

            val total = allBroadcasts.size.toLong()
            val totalPages = (total + size - 1) / size
            val broadcasts = allBroadcasts
                .drop(((page - 1) * size).toInt())
                .take(size)

            mapOf(
                "items" to broadcasts,
                "total" to total,
                "page" to page,
                "size" to size,
                "totalPages" to totalPages
            )
        }
    }

    // ─── Support ──────────────────────────────────────────────────────────

    fun getAllSupportMessages(limit: Int = 500): AdminSupportMessagesResponse {
        return transaction {
            val rows = SupportMessages
                .join(Users, JoinType.LEFT, SupportMessages.userId, Users.userId)
                .selectAll()
                .orderBy(SupportMessages.createdAt, SortOrder.ASC)
                .limit(limit)
                .map { row ->
                    AdminSupportMessageDTO(
                        messageId = row[SupportMessages.messageId],
                        userId = row[SupportMessages.userId],
                        userName = row.getOrNull(Users.fullName),
                        content = row[SupportMessages.content],
                        isFromAdmin = row[SupportMessages.senderType] == "ADMIN",
                        createdAt = row[SupportMessages.createdAt].toString()
                    )
                }
            AdminSupportMessagesResponse(
                items = rows,
                total = rows.size,
                page = 1,
                size = rows.size,
                totalPages = 1
            )
        }
    }

    fun replyToUser(request: AdminReplyRequest, adminId: String): Result<SupportMessageDTO> {
        if (request.content.isBlank()) {
            return Result.failure(IllegalArgumentException("Nội dung phản hồi không được để trống"))
        }
        val message = SupportMessage(
            messageId = UUID.randomUUID().toString(),
            userId = request.userId,
            senderId = adminId,
            senderType = "ADMIN",
            content = request.content,
            isRead = false,
            createdAt = Instant.now()
        )
        val created = supportRepository.create(message)
        return Result.success(SupportMessageDTO.fromEntity(created))
    }

    // ─── Helper ───────────────────────────────────────────────────────────

    private data class TrendBucket(
        val bucketStart: LocalDate,
        val label: String
    )

    private fun collectStatisticsItems(
        startDate: String?,
        endDate: String?,
        game: String?,
        area: String?,
        status: String?,
        search: String?
    ): Pair<List<AdminStatisticsGameItemDTO>, AdminStatisticsSummaryDTO> {
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val parsedStart = parseDateOrThrow(startDate, "startDate")
        val parsedEnd = parseDateOrThrow(endDate, "endDate")
        val today = LocalDate.now(zone)
        val rangeStart = parsedStart ?: today.minusDays(29)
        val rangeEnd = parsedEnd ?: today
        if (rangeEnd.isBefore(rangeStart)) {
            throw IllegalArgumentException("endDate must be greater than or equal to startDate")
        }

        val startInstant = rangeStart.atStartOfDay(zone).toInstant()
        val endExclusive = rangeEnd.plusDays(1).atStartOfDay(zone).toInstant()

        val allGames = statisticsRepository.loadGameMetas()
        val games = allGames
            .filter { matchOptionalFilter(it.name, it.gameId, game) }
            .filter { matchOptionalValue(it.area, area) }
            .filter { matchOptionalValue(it.status, status) }
            .filter { matchSearch(it.name, it.area, search) }

        if (games.isEmpty()) {
            return emptyList<AdminStatisticsGameItemDTO>() to AdminStatisticsSummaryDTO(
                totalGames = 0,
                totalPlays = 0,
                totalPlayers = 0,
                totalRevenue = 0.0
            )
        }

        val gameIds = games.map { it.gameId }.toSet()
        val logs = statisticsRepository.queryPlayLogs(startInstant, endExclusive, gameIds)

        return run {
            val logsByGame = logs.groupBy { it.gameId }
            val totalRevenue = logs.fold(BigDecimal.ZERO) { acc, row -> acc + row.amountCharged }

            val items = games.map { gameMeta ->
                val gameLogs = logsByGame[gameMeta.gameId].orEmpty()
                val plays = gameLogs.size
                val players = gameLogs.map { it.userId }.distinct().size
                val revenue = gameLogs.fold(BigDecimal.ZERO) { acc, row -> acc + row.amountCharged }
                val revenuePerPlay = if (plays == 0) BigDecimal.ZERO else revenue.divide(
                    BigDecimal(plays),
                    2,
                    java.math.RoundingMode.HALF_UP
                )
                val contribution = if (totalRevenue == BigDecimal.ZERO) {
                    0.0
                } else {
                    revenue.multiply(BigDecimal(100))
                        .divide(totalRevenue, 4, java.math.RoundingMode.HALF_UP)
                        .toDouble()
                }

                AdminStatisticsGameItemDTO(
                    gameId = gameMeta.gameId,
                    name = gameMeta.name,
                    area = gameMeta.area,
                    category = gameMeta.category,
                    plays = plays,
                    players = players,
                    revenue = revenue.toDouble(),
                    ticketPrice = gameMeta.ticketPrice.toDouble(),
                    revenuePerPlay = revenuePerPlay.toDouble(),
                    contributionPercent = contribution,
                    status = gameMeta.status
                )
            }.sortedByDescending { it.revenue }

            val summary = AdminStatisticsSummaryDTO(
                totalGames = items.size,
                totalPlays = items.sumOf { it.plays },
                totalPlayers = logs.map { it.userId }.distinct().size,
                totalRevenue = totalRevenue.toDouble()
            )

            items to summary
        }
    }

    private fun normalizePeriod(period: String?): String {
        return when (period?.trim()?.lowercase()) {
            "weekly", "week" -> "weekly"
            "monthly", "month" -> "monthly"
            else -> "daily"
        }
    }

    private fun resolveDateRange(
        period: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
        zone: ZoneId
    ): Pair<LocalDate, LocalDate> {
        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                throw IllegalArgumentException("endDate must be greater than or equal to startDate")
            }
            return startDate to endDate
        }

        val today = LocalDate.now(zone)
        return when (period) {
            "weekly" -> today.minusWeeks(7) to today
            "monthly" -> today.minusMonths(11) to today
            else -> today.minusDays(6) to today
        }
    }

    private fun buildTrendBuckets(period: String, startDate: LocalDate, endDate: LocalDate): List<TrendBucket> {
        return when (period) {
            "weekly" -> {
                var cursor = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val final = endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val buckets = mutableListOf<TrendBucket>()
                while (!cursor.isAfter(final)) {
                    val label = "Week ${cursor.dayOfMonth}/${cursor.monthValue}"
                    buckets += TrendBucket(cursor, label)
                    cursor = cursor.plusWeeks(1)
                }
                buckets
            }

            "monthly" -> {
                var cursor = startDate.withDayOfMonth(1)
                val final = endDate.withDayOfMonth(1)
                val buckets = mutableListOf<TrendBucket>()
                while (!cursor.isAfter(final)) {
                    buckets += TrendBucket(cursor, "Th${cursor.monthValue}/${cursor.year}")
                    cursor = cursor.plusMonths(1)
                }
                buckets
            }

            else -> {
                var cursor = startDate
                val buckets = mutableListOf<TrendBucket>()
                while (!cursor.isAfter(endDate)) {
                    buckets += TrendBucket(cursor, "${cursor.dayOfMonth}/${cursor.monthValue}")
                    cursor = cursor.plusDays(1)
                }
                buckets
            }
        }
    }

    private fun parseDateOrThrow(value: String?, field: String): LocalDate? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value.trim())
        } catch (_: Exception) {
            throw IllegalArgumentException("Invalid $field format. Expected YYYY-MM-DD.")
        }
    }

    private fun normalizeText(value: String?): String {
        if (value.isNullOrBlank()) return ""
        val normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
        return normalized.replace("\\p{Mn}+".toRegex(), "")
            .lowercase()
            .trim()
    }

    private fun isAllFilter(value: String?): Boolean {
        val normalized = normalizeText(value)
        return normalized.isBlank() || normalized == "all" || normalized.startsWith("tat ca")
    }

    private fun matchOptionalValue(value: String?, filter: String?): Boolean {
        if (isAllFilter(filter)) return true
        return normalizeText(value) == normalizeText(filter)
    }

    private fun matchOptionalFilter(name: String, id: String, filter: String?): Boolean {
        if (isAllFilter(filter)) return true
        val f = normalizeText(filter)
        return normalizeText(id) == f || normalizeText(name).contains(f)
    }

    private fun matchSearch(name: String, area: String?, search: String?): Boolean {
        if (search.isNullOrBlank()) return true
        val keyword = normalizeText(search)
        return normalizeText(name).contains(keyword) || normalizeText(area).contains(keyword)
    }

    private fun generateToken(accountId: String, adminId: String, role: String): String {
        return JWT.create()
            .withAudience(JWT_AUDIENCE)
            .withIssuer(JWT_ISSUER)
            .withClaim("accountId", accountId)
            .withClaim("userId", adminId)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + JWT_VALIDITY_MS))
            .sign(Algorithm.HMAC256(JWT_SECRET))
    }
}
