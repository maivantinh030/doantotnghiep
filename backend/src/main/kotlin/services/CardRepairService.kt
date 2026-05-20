package com.park.services

import com.park.entities.Card
import com.park.repositories.CardRepository
import com.park.repositories.ICardRepository
import com.park.repositories.IUserRepository
import com.park.repositories.UserRepository
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

class CardRepairService(
    private val cardRepository: ICardRepository = CardRepository(),
    private val userRepository: IUserRepository = UserRepository()
) {

    /**
     * Quét toàn bộ thẻ, tìm và báo cáo mọi trạng thái không hợp lệ.
     * Không tự động sửa — chỉ trả về danh sách lỗi để review.
     */
    fun diagnose(): RepairReport {
        val issues = mutableListOf<CardIssue>()
        val allCards = cardRepository.findAll()
        val userIds = userRepository.findAllIds().toSet()

        for (card in allCards) {
            issues += detectIssues(card, userIds)
        }

        return RepairReport(
            scannedAt = Instant.now(),
            totalCards = cardRepository.countAll().toInt(),
            issuesFound = issues,
            fixedCount = 0,
            skippedCount = issues.size,
            errors = emptyList()
        )
    }

    private fun detectIssues(card: Card, userIds: Set<String>): List<CardIssue> {
        val list = mutableListOf<CardIssue>()
        val cardId = card.cardId
        val uid = card.cardId // physicalCardUid not in entity, fallback to cardId

        if (card.status == "ACTIVE" && card.userId == null) {
            list.add(CardIssue(
                cardId = cardId,
                physicalCardUid = uid,
                issueType = IssueType.ACTIVE_WITHOUT_USER,
                description = "Thẻ ACTIVE nhưng userId = null",
                fixAction = "Reset về AVAILABLE, xóa depositStatus"
            ))
        }
        if (card.userId != null && card.userId !in userIds) {
            list.add(CardIssue(
                cardId = cardId,
                physicalCardUid = uid,
                issueType = IssueType.ORPHANED_USER_REFERENCE,
                description = "userId '${card.userId}' không tồn tại trong bảng users",
                fixAction = "Unlink userId, reset thẻ về AVAILABLE"
            ))
        }
        if (card.depositStatus == "PAID" && card.status != "ACTIVE") {
            list.add(CardIssue(
                cardId = cardId,
                physicalCardUid = uid,
                issueType = IssueType.DEPOSIT_MISMATCH,
                description = "depositStatus=PAID nhưng status=${card.status}",
                fixAction = "Nếu AVAILABLE: reset deposit về NONE. Nếu BLOCKED: cập nhật sang FORFEITED"
            ))
        }
        if (card.status == "BLOCKED" && card.depositStatus == "PAID") {
            list.add(CardIssue(
                cardId = cardId,
                physicalCardUid = uid,
                issueType = IssueType.BLOCKED_WITH_PAID_DEPOSIT,
                description = "Thẻ BLOCKED nhưng cọc chưa được tịch thu (depositStatus=PAID)",
                fixAction = "Cập nhật depositStatus sang FORFEITED"
            ))
        }
        if (card.status == "AVAILABLE" && card.userId != null) {
            list.add(CardIssue(
                cardId = cardId,
                physicalCardUid = uid,
                issueType = IssueType.AVAILABLE_WITH_USER,
                description = "Thẻ AVAILABLE nhưng userId='${card.userId}' vẫn còn",
                fixAction = "Xóa userId (set NULL)"
            ))
        }
        return list
    }

    /**
     * Tự động sửa tất cả lỗi được phát hiện.
     */
    fun repairAll(): RepairReport {
        val issues = diagnose().issuesFound
        if (issues.isEmpty()) {
            return RepairReport(
                scannedAt = Instant.now(),
                totalCards = cardRepository.countAll().toInt(),
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
                applyFix(issue)
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
            totalCards = cardRepository.countAll().toInt(),
            issuesFound = issues,
            fixedCount = fixedCount,
            skippedCount = issues.size - fixedCount,
            errors = errors
        )
    }

    /**
     * Sửa lỗi theo loại cụ thể.
     */
    fun repairByType(type: IssueType): RepairReport {
        val allIssues = diagnose().issuesFound
        val targeted = allIssues.filter { it.issueType == type }
        var fixedCount = 0
        val errors = mutableListOf<String>()

        for (issue in targeted) {
            try {
                applyFix(issue)
                fixedCount++
            } catch (e: Exception) {
                errors.add("Thẻ ${issue.cardId}: ${e.message}")
            }
        }

        return RepairReport(
            scannedAt = Instant.now(),
            totalCards = cardRepository.countAll().toInt(),
            issuesFound = targeted,
            fixedCount = fixedCount,
            skippedCount = targeted.size - fixedCount,
            errors = errors
        )
    }

    private fun applyFix(issue: CardIssue) {
        when (issue.issueType) {
            IssueType.ACTIVE_WITHOUT_USER -> fixActiveWithoutUser(issue.cardId)
            IssueType.ORPHANED_USER_REFERENCE -> fixOrphanedUserReference(issue.cardId)
            IssueType.DEPOSIT_MISMATCH -> fixDepositMismatch(issue.cardId)
            IssueType.BLOCKED_WITH_PAID_DEPOSIT -> fixBlockedWithPaidDeposit(issue.cardId)
            IssueType.AVAILABLE_WITH_USER -> fixAvailableWithUser(issue.cardId)
        }
    }

    /** Thẻ ACTIVE nhưng không có userId → reset về AVAILABLE */
    private fun fixActiveWithoutUser(cardId: String) {
        cardRepository.update(cardId, mapOf(
            "status" to "AVAILABLE",
            "userId" to null,
            "depositAmount" to BigDecimal.ZERO,
            "depositStatus" to "NONE",
            "issuedAt" to null
        ))
    }

    /** userId trỏ đến user không tồn tại → unlink */
    private fun fixOrphanedUserReference(cardId: String) {
        cardRepository.update(cardId, mapOf(
            "userId" to null,
            "status" to "AVAILABLE",
            "depositAmount" to BigDecimal.ZERO,
            "depositStatus" to "NONE",
            "issuedAt" to null
        ))
    }

    /** depositStatus=PAID nhưng thẻ không ACTIVE */
    private fun fixDepositMismatch(cardId: String) {
        val card = cardRepository.findById(cardId) ?: return
        val newDepositStatus = if (card.status == "BLOCKED") "FORFEITED" else "NONE"
        val updates = mutableMapOf<String, Any?>(
            "depositStatus" to newDepositStatus
        )
        if (newDepositStatus == "NONE") {
            updates["depositAmount"] = BigDecimal.ZERO
        }
        cardRepository.update(cardId, updates)
    }

    /** Thẻ BLOCKED nhưng cọc vẫn PAID → đổi sang FORFEITED */
    private fun fixBlockedWithPaidDeposit(cardId: String) {
        cardRepository.update(cardId, mapOf("depositStatus" to "FORFEITED"))
    }

    /** Thẻ AVAILABLE nhưng vẫn có userId → xóa liên kết */
    private fun fixAvailableWithUser(cardId: String) {
        cardRepository.update(cardId, mapOf("userId" to null))
    }

    /**
     * In báo cáo tóm tắt ra console.
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
