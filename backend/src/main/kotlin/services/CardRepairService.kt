package com.park.services

import com.park.database.tables.BalanceTransactions
import com.park.database.tables.Cards
import com.park.database.tables.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant

// ─────────────────────────────────────────────────────────────────────────────
// Data classes cho kết quả chẩn đoán
// ─────────────────────────────────────────────────────────────────────────────

data class CardIssue(
    val cardId: String,
    val physicalCardUid: String,
    val issueType: IssueType,
    val description: String,
    val fixAction: String
)

enum class IssueType {
    ACTIVE_WITHOUT_USER,       // Thẻ ACTIVE nhưng userId = null
    DEPOSIT_MISMATCH,          // depositStatus=PAID nhưng thẻ không ACTIVE
    ORPHANED_USER_REFERENCE,   // userId trỏ đến user không tồn tại
    BLOCKED_WITH_PAID_DEPOSIT, // Thẻ BLOCKED nhưng depositStatus vẫn PAID (chưa tịch thu)
    AVAILABLE_WITH_USER,       // Thẻ AVAILABLE nhưng có userId (trạng thái mâu thuẫn)
}

data class RepairReport(
    val scannedAt: Instant,
    val totalCards: Int,
    val issuesFound: List<CardIssue>,
    val fixedCount: Int,
    val skippedCount: Int,
    val errors: List<String>
)

// ─────────────────────────────────────────────────────────────────────────────
// CardRepairService — chẩn đoán và sửa lỗi trạng thái thẻ trong DB
// ─────────────────────────────────────────────────────────────────────────────

class CardRepairService {

    /**
     * Quét toàn bộ thẻ, tìm và báo cáo mọi trạng thái không hợp lệ.
     * Không tự động sửa — chỉ trả về danh sách lỗi để review.
     */
    fun diagnose(): RepairReport {
        val issues = mutableListOf<CardIssue>()

        transaction {
            val allCards = Cards.selectAll().toList()
            val userIds = Users.selectAll().map { it[Users.userId] }.toSet()

            for (row in allCards) {
                val cardId = row[Cards.cardId]
                val uid = row[Cards.physicalCardUid]
                val status = row[Cards.status]
                val userId = row[Cards.userId]
                val depositStatus = row[Cards.depositStatus]

                // Thẻ ACTIVE nhưng không có userId
                if (status == "ACTIVE" && userId == null) {
                    issues.add(CardIssue(
                        cardId = cardId,
                        physicalCardUid = uid,
                        issueType = IssueType.ACTIVE_WITHOUT_USER,
                        description = "Thẻ ACTIVE nhưng userId = null",
                        fixAction = "Reset về AVAILABLE, xóa depositStatus"
                    ))
                }

                // userId trỏ đến user không tồn tại
                if (userId != null && userId !in userIds) {
                    issues.add(CardIssue(
                        cardId = cardId,
                        physicalCardUid = uid,
                        issueType = IssueType.ORPHANED_USER_REFERENCE,
                        description = "userId '$userId' không tồn tại trong bảng users",
                        fixAction = "Unlink userId, reset thẻ về AVAILABLE"
                    ))
                }

                // depositStatus=PAID nhưng thẻ không ACTIVE
                if (depositStatus == "PAID" && status != "ACTIVE") {
                    issues.add(CardIssue(
                        cardId = cardId,
                        physicalCardUid = uid,
                        issueType = IssueType.DEPOSIT_MISMATCH,
                        description = "depositStatus=PAID nhưng status=$status",
                        fixAction = "Nếu AVAILABLE: reset deposit về NONE. Nếu BLOCKED: cập nhật sang FORFEITED"
                    ))
                }

                // Thẻ BLOCKED nhưng depositStatus vẫn PAID
                if (status == "BLOCKED" && depositStatus == "PAID") {
                    issues.add(CardIssue(
                        cardId = cardId,
                        physicalCardUid = uid,
                        issueType = IssueType.BLOCKED_WITH_PAID_DEPOSIT,
                        description = "Thẻ BLOCKED nhưng cọc chưa được tịch thu (depositStatus=PAID)",
                        fixAction = "Cập nhật depositStatus sang FORFEITED"
                    ))
                }

                // Thẻ AVAILABLE nhưng vẫn có userId
                if (status == "AVAILABLE" && userId != null) {
                    issues.add(CardIssue(
                        cardId = cardId,
                        physicalCardUid = uid,
                        issueType = IssueType.AVAILABLE_WITH_USER,
                        description = "Thẻ AVAILABLE nhưng userId='$userId' vẫn còn",
                        fixAction = "Xóa userId (set NULL)"
                    ))
                }
            }
        }

        return RepairReport(
            scannedAt = Instant.now(),
            totalCards = transaction { Cards.selectAll().count().toInt() },
            issuesFound = issues,
            fixedCount = 0,
            skippedCount = issues.size,
            errors = emptyList()
        )
    }

    /**
     * Tự động sửa tất cả lỗi được phát hiện.
     * Trả về báo cáo kết quả sau khi sửa.
     */
    fun repairAll(): RepairReport {
        val issues = diagnose().issuesFound
        if (issues.isEmpty()) {
            return RepairReport(
                scannedAt = Instant.now(),
                totalCards = transaction { Cards.selectAll().count().toInt() },
                issuesFound = emptyList(),
                fixedCount = 0,
                skippedCount = 0,
                errors = emptyList()
            )
        }

        var fixedCount = 0
        val errors = mutableListOf<String>()

        for (issue in issues) {
            try {
                when (issue.issueType) {
                    IssueType.ACTIVE_WITHOUT_USER -> fixActiveWithoutUser(issue.cardId)
                    IssueType.ORPHANED_USER_REFERENCE -> fixOrphanedUserReference(issue.cardId)
                    IssueType.DEPOSIT_MISMATCH -> fixDepositMismatch(issue.cardId)
                    IssueType.BLOCKED_WITH_PAID_DEPOSIT -> fixBlockedWithPaidDeposit(issue.cardId)
                    IssueType.AVAILABLE_WITH_USER -> fixAvailableWithUser(issue.cardId)
                }
                fixedCount++
                println("✅ [CardRepair] Đã sửa ${issue.issueType} cho thẻ ${issue.cardId}")
            } catch (e: Exception) {
                val msg = "❌ [CardRepair] Lỗi khi sửa ${issue.issueType} cho thẻ ${issue.cardId}: ${e.message}"
                errors.add(msg)
                println(msg)
            }
        }

        return RepairReport(
            scannedAt = Instant.now(),
            totalCards = transaction { Cards.selectAll().count().toInt() },
            issuesFound = issues,
            fixedCount = fixedCount,
            skippedCount = issues.size - fixedCount,
            errors = errors
        )
    }

    /**
     * Sửa lỗi theo loại cụ thể — dùng khi chỉ muốn sửa 1 loại vấn đề.
     */
    fun repairByType(type: IssueType): RepairReport {
        val allIssues = diagnose().issuesFound
        val targeted = allIssues.filter { it.issueType == type }
        var fixedCount = 0
        val errors = mutableListOf<String>()

        for (issue in targeted) {
            try {
                when (type) {
                    IssueType.ACTIVE_WITHOUT_USER -> fixActiveWithoutUser(issue.cardId)
                    IssueType.ORPHANED_USER_REFERENCE -> fixOrphanedUserReference(issue.cardId)
                    IssueType.DEPOSIT_MISMATCH -> fixDepositMismatch(issue.cardId)
                    IssueType.BLOCKED_WITH_PAID_DEPOSIT -> fixBlockedWithPaidDeposit(issue.cardId)
                    IssueType.AVAILABLE_WITH_USER -> fixAvailableWithUser(issue.cardId)
                }
                fixedCount++
            } catch (e: Exception) {
                errors.add("Thẻ ${issue.cardId}: ${e.message}")
            }
        }

        return RepairReport(
            scannedAt = Instant.now(),
            totalCards = transaction { Cards.selectAll().count().toInt() },
            issuesFound = targeted,
            fixedCount = fixedCount,
            skippedCount = targeted.size - fixedCount,
            errors = errors
        )
    }

    // ── Fix methods riêng lẻ ─────────────────────────────────────────────────

    /** Thẻ ACTIVE nhưng không có userId → reset về AVAILABLE */
    private fun fixActiveWithoutUser(cardId: String) = transaction {
        Cards.update({ Cards.cardId eq cardId }) {
            it[status] = "AVAILABLE"
            it[userId] = null
            it[depositAmount] = BigDecimal.ZERO
            it[depositStatus] = "NONE"
            it[issuedAt] = null
            it[updatedAt] = Instant.now()
        }
    }

    /** userId trỏ đến user không tồn tại → unlink */
    private fun fixOrphanedUserReference(cardId: String) = transaction {
        Cards.update({ Cards.cardId eq cardId }) {
            it[userId] = null
            it[status] = "AVAILABLE"
            it[depositAmount] = BigDecimal.ZERO
            it[depositStatus] = "NONE"
            it[issuedAt] = null
            it[updatedAt] = Instant.now()
        }
    }

    /** depositStatus=PAID nhưng thẻ không ACTIVE */
    private fun fixDepositMismatch(cardId: String) = transaction {
        val row = Cards.selectAll().where { Cards.cardId eq cardId }.singleOrNull() ?: return@transaction
        val newDepositStatus = if (row[Cards.status] == "BLOCKED") "FORFEITED" else "NONE"
        Cards.update({ Cards.cardId eq cardId }) {
            it[depositStatus] = newDepositStatus
            if (newDepositStatus == "NONE") it[depositAmount] = BigDecimal.ZERO
            it[updatedAt] = Instant.now()
        }
    }

    /** Thẻ BLOCKED nhưng cọc vẫn PAID → đổi sang FORFEITED */
    private fun fixBlockedWithPaidDeposit(cardId: String) = transaction {
        Cards.update({ Cards.cardId eq cardId }) {
            it[depositStatus] = "FORFEITED"
            it[updatedAt] = Instant.now()
        }
    }

    /** Thẻ AVAILABLE nhưng vẫn có userId → xóa liên kết */
    private fun fixAvailableWithUser(cardId: String) = transaction {
        Cards.update({ Cards.cardId eq cardId }) {
            it[userId] = null
            it[updatedAt] = Instant.now()
        }
    }

    // ── Thống kê nhanh ───────────────────────────────────────────────────────

    /**
     * In báo cáo tóm tắt ra console — dùng khi chạy thủ công từ admin tool.
     */
    fun printSummary(report: RepairReport) {
        println("═══════════════════════════════════════════")
        println("  CARD REPAIR REPORT — ${report.scannedAt}")
        println("═══════════════════════════════════════════")
        println("  Tổng số thẻ quét:   ${report.totalCards}")
        println("  Số lỗi tìm thấy:    ${report.issuesFound.size}")
        println("  Đã sửa:             ${report.fixedCount}")
        println("  Bỏ qua/lỗi:        ${report.skippedCount}")
        if (report.issuesFound.isEmpty()) {
            println("  ✅ Không tìm thấy lỗi nào!")
        } else {
            println("\n  Chi tiết lỗi:")
            report.issuesFound.forEach { issue ->
                println("  ─ [${issue.issueType}] ${issue.cardId} (${issue.physicalCardUid})")
                println("    Mô tả:   ${issue.description}")
                println("    Hành động: ${issue.fixAction}")
            }
        }
        if (report.errors.isNotEmpty()) {
            println("\n  Lỗi khi sửa:")
            report.errors.forEach { println("  ✗ $it") }
        }
        println("═══════════════════════════════════════════")
    }
}
