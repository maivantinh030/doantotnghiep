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
import java.time.ZoneId
import java.util.*

class AdminService(
    private val accountRepository: IAccountRepository = AccountRepository(),
    private val adminRepository: IAdminRepository = AdminRepository(),
    private val supportRepository: ISupportRepository = SupportRepository(),
    private val balanceTransactionRepository: IBalanceTransactionRepository = BalanceTransactionRepository(),
    private val gameRepository: IGameRepository = GameRepository(),
    private val notificationService: NotificationService = NotificationService()
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
            if (result != null) Result.success(result)
            else Result.failure(IllegalArgumentException("Số dư không đủ hoặc người dùng không tồn tại"))
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
