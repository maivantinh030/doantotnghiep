package com.park

import com.park.dto.*
import com.park.entities.BalanceTransaction
import com.park.entities.Card
import com.park.entities.User
import com.park.repositories.IBalanceTransactionRepository
import com.park.repositories.ICardRepository
import com.park.repositories.IUserRepository
import com.park.services.CardService
import kotlin.test.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// ─────────────────────────────────────────────────────────────────────────────
// Fake in-memory repositories — không cần DB thật khi chạy test
// ─────────────────────────────────────────────────────────────────────────────

class FakeCardRepository : ICardRepository {
    val store = ConcurrentHashMap<String, Card>()

    override fun create(card: Card): Card {
        store[card.cardId] = card
        return card
    }

    override fun findById(cardId: String): Card? = store[cardId]

    override fun findByPhysicalUid(uid: String): Card? =
        store.values.find { it.cardId == uid }

    override fun findByUserId(userId: String): List<Card> =
        store.values.filter { it.userId == userId }

    override fun findAvailable(): List<Card> =
        store.values.filter { it.status == "AVAILABLE" }

    override fun update(cardId: String, updates: Map<String, Any?>): Boolean {
        val card = store[cardId] ?: return false
        var updated = card
        updates.forEach { (key, value) ->
            updated = when (key) {
                "userId"        -> updated.copy(userId = value as? String)
                "cardName"      -> updated.copy(cardName = value as? String)
                "status"        -> updated.copy(status = value as String)
                "depositAmount" -> updated.copy(depositAmount = value as BigDecimal)
                "depositStatus" -> updated.copy(depositStatus = value as String)
                "issuedAt"      -> updated.copy(issuedAt = value as? Instant)
                "blockedAt"     -> updated.copy(blockedAt = value as? Instant)
                "blockedReason" -> updated.copy(blockedReason = value as? String)
                "lastUsedAt"    -> updated.copy(lastUsedAt = value as? Instant)
                else            -> updated
            }
        }
        store[cardId] = updated.copy(updatedAt = Instant.now())
        return true
    }

    override fun delete(cardId: String): Boolean = store.remove(cardId) != null
}

class FakeUserRepository : IUserRepository {
    val store = ConcurrentHashMap<String, User>()

    fun addUser(user: User) { store[user.userId] = user }

    override fun create(dto: CreateUserDTO): User {
        val user = User(
            userId = UUID.randomUUID().toString(),
            accountId = dto.accountId,
            fullName = dto.fullName,
            email = dto.email,
            dateOfBirth = null,
            gender = dto.gender,
            currentBalance = BigDecimal.ZERO,
            avatarUrl = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        store[user.userId] = user
        return user
    }

    override fun findById(userId: String): User? = store[userId]
    override fun findByAccountId(accountId: String): User? =
        store.values.find { it.accountId == accountId }
    override fun findByEmail(email: String): User? =
        store.values.find { it.email == email }
    override fun existsByEmail(email: String): Boolean =
        store.values.any { it.email == email }

    override fun update(userId: String, updates: Map<String, Any?>): Boolean {
        val user = store[userId] ?: return false
        var updated = user
        updates.forEach { (key, value) ->
            updated = when (key) {
                "currentBalance" -> updated.copy(currentBalance = value as BigDecimal)
                "fullName"       -> updated.copy(fullName = value as? String)
                "email"          -> updated.copy(email = value as? String)
                else             -> updated
            }
        }
        store[userId] = updated
        return true
    }
}

class FakeBalanceTransactionRepository : IBalanceTransactionRepository {
    val store = mutableListOf<BalanceTransaction>()

    override fun create(tx: BalanceTransaction): BalanceTransaction {
        store.add(tx)
        return tx
    }

    override fun findByUserId(userId: String, limit: Int, offset: Long): List<BalanceTransaction> =
        store.filter { it.userId == userId }.drop(offset.toInt()).take(limit)

    override fun findByUserIdAndType(userId: String, type: String, limit: Int, offset: Long): List<BalanceTransaction> =
        store.filter { it.userId == userId && it.type == type }.drop(offset.toInt()).take(limit)

    override fun countByUserId(userId: String): Long =
        store.count { it.userId == userId }.toLong()
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun makeTestUser(
    userId: String = "user-001",
    balance: BigDecimal = BigDecimal("100.00")
): User = User(
    userId = userId,
    accountId = "acc-001",
    fullName = "Nguyen Van A",
    email = "test@park.com",
    dateOfBirth = null,
    gender = "MALE",
    currentBalance = balance,
    avatarUrl = null,
    createdAt = Instant.now(),
    updatedAt = Instant.now()
)

private fun buildService(
    cardRepo: FakeCardRepository = FakeCardRepository(),
    userRepo: FakeUserRepository = FakeUserRepository(),
    txRepo: FakeBalanceTransactionRepository = FakeBalanceTransactionRepository()
): CardService = CardService(cardRepo, userRepo, txRepo)

// ─────────────────────────────────────────────────────────────────────────────
// Test Suite: Luồng tạo thẻ và ghi vào database
// ─────────────────────────────────────────────────────────────────────────────

class CardCreationFlowTest {

    // ── 1. Đăng ký thẻ trắng ─────────────────────────────────────────────────

    @Test
    fun `registerCard - tạo thẻ mới thành công, status AVAILABLE`() {
        val cardRepo = FakeCardRepository()
        val svc = buildService(cardRepo)

        val result = svc.registerCard(RegisterCardRequest(cardId = "CARD-001", cardName = "Thẻ đỏ"))

        assertTrue(result.isSuccess, "Phải đăng ký thành công")
        val card = result.getOrThrow()
        assertEquals("AVAILABLE", card.status)
        assertEquals("CARD-001", card.cardId)
        assertEquals("0", card.depositAmount)
        assertEquals("NONE", card.depositStatus)
        assertNull(card.userId)
        // Kiểm tra đã ghi vào store
        assertEquals(1, cardRepo.store.size)
    }

    @Test
    fun `registerCard - UID trùng thì trả về failure`() {
        val cardRepo = FakeCardRepository()
        val svc = buildService(cardRepo)

        svc.registerCard(RegisterCardRequest(cardId = "CARD-DUP"))
        val result = svc.registerCard(RegisterCardRequest(cardId = "CARD-DUP"))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("đã tồn tại") == true)
        // DB chỉ có 1 bản ghi
        assertEquals(1, cardRepo.store.size)
    }

    @Test
    fun `registerCard - cardName null vẫn tạo được`() {
        val svc = buildService()
        val result = svc.registerCard(RegisterCardRequest(cardId = "CARD-NONAME"))
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow().cardName)
    }

    // ── 2. Phát hành thẻ cho khách ────────────────────────────────────────────

    @Test
    fun `issueCard - phát hành thành công, trạng thái ACTIVE và ghi deposit`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val txRepo = FakeBalanceTransactionRepository()
        val svc = buildService(cardRepo, userRepo, txRepo)

        // Chuẩn bị: thẻ + user
        val regResult = svc.registerCard(RegisterCardRequest(cardId = "CARD-A"))
        val cardId = regResult.getOrThrow().cardId
        val user = makeTestUser("user-001")
        userRepo.addUser(user)

        val issueResult = svc.issueCard(
            IssueCardRequest(cardId = cardId, userId = "user-001", depositAmount = "50000", cardName = "Thẻ xanh"),
            staffId = "staff-001"
        )

        assertTrue(issueResult.isSuccess, "Phát hành phải thành công")
        val card = issueResult.getOrThrow()
        assertEquals("ACTIVE", card.status)
        assertEquals("user-001", card.userId)
        assertEquals("50000", card.depositAmount)
        assertEquals("PAID", card.depositStatus)
        assertNotNull(card.issuedAt)

        // Kiểm tra giao dịch DEPOSIT_PAID đã được ghi vào DB
        val depositTxs = txRepo.store.filter { it.type == "DEPOSIT_PAID" }
        assertEquals(1, depositTxs.size, "Phải có 1 giao dịch DEPOSIT_PAID")
        assertEquals("user-001", depositTxs[0].userId)
        assertEquals("CARD", depositTxs[0].referenceType)
        assertEquals(cardId, depositTxs[0].referenceId)
    }

    @Test
    fun `issueCard - thẻ không tồn tại thì failure`() {
        val svc = buildService()
        val result = svc.issueCard(
            IssueCardRequest(cardId = "ghost-id", userId = "u1", depositAmount = "0"),
            staffId = "staff-001"
        )
        assertTrue(result.isFailure)
        assertIs<NoSuchElementException>(result.exceptionOrNull())
    }

    @Test
    fun `issueCard - thẻ đã ACTIVE thì không thể phát hành lại`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val svc = buildService(cardRepo, userRepo)

        val regResult = svc.registerCard(RegisterCardRequest(cardId = "CARD-B"))
        val cardId = regResult.getOrThrow().cardId
        val user = makeTestUser("user-001")
        userRepo.addUser(user)

        // Phát hành lần 1
        svc.issueCard(IssueCardRequest(cardId, "user-001", "50000"), "staff-001")
        // Phát hành lần 2 cùng thẻ
        val result = svc.issueCard(IssueCardRequest(cardId, "user-001", "50000"), "staff-001")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("không ở trạng thái") == true)
    }

    @Test
    fun `issueCard - userId không tồn tại thì failure`() {
        val cardRepo = FakeCardRepository()
        val svc = buildService(cardRepo)

        val regResult = svc.registerCard(RegisterCardRequest(cardId = "CARD-C"))
        val cardId = regResult.getOrThrow().cardId

        val result = svc.issueCard(
            IssueCardRequest(cardId, "ghost-user", "50000"),
            "staff-001"
        )
        assertTrue(result.isFailure)
        assertIs<NoSuchElementException>(result.exceptionOrNull())
    }

    @Test
    fun `issueCard - depositAmount âm thì failure`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val svc = buildService(cardRepo, userRepo)

        val cardId = svc.registerCard(RegisterCardRequest("CARD-D")).getOrThrow().cardId
        userRepo.addUser(makeTestUser("user-002"))

        val result = svc.issueCard(IssueCardRequest(cardId, "user-002", "-100"), "staff-001")
        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    // ── 3. Trả thẻ ────────────────────────────────────────────────────────────

    @Test
    fun `returnCard - trả thẻ thành công, unlink và hoàn tiền cọc`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val txRepo = FakeBalanceTransactionRepository()
        val svc = buildService(cardRepo, userRepo, txRepo)

        val cardId = svc.registerCard(RegisterCardRequest("CARD-E")).getOrThrow().cardId
        userRepo.addUser(makeTestUser("user-003", balance = BigDecimal("200.00")))
        svc.issueCard(IssueCardRequest(cardId, "user-003", "50000"), "staff-001")

        val returnResult = svc.returnCard(cardId, "staff-001")

        assertTrue(returnResult.isSuccess, "Trả thẻ phải thành công")
        val summary = returnResult.getOrThrow()
        assertEquals(cardId, summary["cardId"])

        // Card phải về AVAILABLE và unlink
        val card = cardRepo.findById(cardId)!!
        assertEquals("AVAILABLE", card.status)
        assertNull(card.userId)
        assertEquals("NONE", card.depositStatus)
        assertEquals(BigDecimal.ZERO, card.depositAmount)

        // Phải có giao dịch DEPOSIT_REFUND
        val refundTxs = txRepo.store.filter { it.type == "DEPOSIT_REFUND" }
        assertEquals(1, refundTxs.size)
    }

    @Test
    fun `returnCard - thẻ chưa liên kết thì failure`() {
        val cardRepo = FakeCardRepository()
        val svc = buildService(cardRepo)
        val cardId = svc.registerCard(RegisterCardRequest("CARD-F")).getOrThrow().cardId

        val result = svc.returnCard(cardId, "staff-001")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("chưa được liên kết") == true)
    }

    @Test
    fun `returnCard - số dư dương thì ghi giao dịch REFUND`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val txRepo = FakeBalanceTransactionRepository()
        val svc = buildService(cardRepo, userRepo, txRepo)

        val cardId = svc.registerCard(RegisterCardRequest("CARD-G")).getOrThrow().cardId
        userRepo.addUser(makeTestUser("user-004", balance = BigDecimal("150.00")))
        svc.issueCard(IssueCardRequest(cardId, "user-004", "0"), "staff-001")

        svc.returnCard(cardId, "staff-001")

        val refundTxs = txRepo.store.filter { it.type == "REFUND" }
        assertEquals(1, refundTxs.size, "Phải có giao dịch REFUND số dư")
        assertEquals(BigDecimal("150.00"), refundTxs[0].amount.abs())
    }

    // ── 4. Khóa thẻ ───────────────────────────────────────────────────────────

    @Test
    fun `blockCard - khóa thẻ thành công và tịch thu cọc`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val txRepo = FakeBalanceTransactionRepository()
        val svc = buildService(cardRepo, userRepo, txRepo)

        val cardId = svc.registerCard(RegisterCardRequest("CARD-H")).getOrThrow().cardId
        userRepo.addUser(makeTestUser("user-005"))
        svc.issueCard(IssueCardRequest(cardId, "user-005", "30000"), "staff-001")

        val blockResult = svc.blockCard(cardId, "Khách mất thẻ", "staff-001")

        assertTrue(blockResult.isSuccess)
        val card = blockResult.getOrThrow()
        assertEquals("BLOCKED", card.status)
        assertEquals("FORFEITED", card.depositStatus)
        assertEquals("Khách mất thẻ", card.blockedReason)
        assertNotNull(card.blockedAt)

        // Phải có giao dịch DEPOSIT_FORFEITED
        val forfeitTxs = txRepo.store.filter { it.type == "DEPOSIT_FORFEITED" }
        assertEquals(1, forfeitTxs.size)
    }

    @Test
    fun `blockCard - thẻ đã BLOCKED thì không khóa lại`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val svc = buildService(cardRepo, userRepo)

        val cardId = svc.registerCard(RegisterCardRequest("CARD-I")).getOrThrow().cardId
        userRepo.addUser(makeTestUser("user-006"))
        svc.issueCard(IssueCardRequest(cardId, "user-006", "10000"), "staff-001")
        svc.blockCard(cardId, null, "staff-001")

        val result = svc.blockCard(cardId, null, "staff-001")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("đã bị khóa") == true)
    }

    // ── 5. Quẹt thẻ (terminal) ────────────────────────────────────────────────

    @Test
    fun `processCardTap - thẻ ACTIVE quẹt thành công và cập nhật lastUsedAt`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val svc = buildService(cardRepo, userRepo)

        val cardId = svc.registerCard(RegisterCardRequest("CARD-J")).getOrThrow().cardId
        userRepo.addUser(makeTestUser("user-007"))
        svc.issueCard(IssueCardRequest(cardId, "user-007", "0"), "staff-001")

        val tapResult = svc.processCardTap("CARD-J")

        assertTrue(tapResult.isSuccess)
        val card = tapResult.getOrThrow()
        assertEquals("ACTIVE", card.status)
        assertNotNull(card.lastUsedAt)
    }

    @Test
    fun `processCardTap - thẻ BLOCKED thì từ chối`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val svc = buildService(cardRepo, userRepo)

        val cardId = svc.registerCard(RegisterCardRequest("CARD-K")).getOrThrow().cardId
        userRepo.addUser(makeTestUser("user-008"))
        svc.issueCard(IssueCardRequest(cardId, "user-008", "0"), "staff-001")
        svc.blockCard(cardId, null, "staff-001")

        val result = svc.processCardTap("CARD-K")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("bị khóa") == true)
    }

    @Test
    fun `processCardTap - thẻ AVAILABLE (chưa liên kết) thì từ chối`() {
        val cardRepo = FakeCardRepository()
        val svc = buildService(cardRepo)
        svc.registerCard(RegisterCardRequest("CARD-L"))

        val result = svc.processCardTap("CARD-L")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("chưa được liên kết") == true)
    }

    @Test
    fun `processCardTap - UID không tồn tại thì failure`() {
        val svc = buildService()
        val result = svc.processCardTap("CARD-UNKNOWN")
        assertTrue(result.isFailure)
        assertIs<NoSuchElementException>(result.exceptionOrNull())
    }

    // ── 6. Toàn bộ vòng đời thẻ (full lifecycle flow) ────────────────────────

    @Test
    fun `full lifecycle - tạo → phát hành → quẹt → trả thẻ, DB nhất quán`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val txRepo = FakeBalanceTransactionRepository()
        val svc = buildService(cardRepo, userRepo, txRepo)

        // Bước 1: Staff đăng ký thẻ trắng
        val cardId = svc.registerCard(RegisterCardRequest("CARD-FULL", "Thẻ thăm quan")).getOrThrow().cardId
        assertEquals(1, cardRepo.store.size)
        assertEquals("AVAILABLE", cardRepo.findById(cardId)!!.status)

        // Bước 2: User đăng ký tài khoản (thêm trực tiếp vào fake repo)
        val user = makeTestUser("user-FULL", balance = BigDecimal("0"))
        userRepo.addUser(user)

        // Bước 3: Staff phát hành thẻ cho user
        svc.issueCard(IssueCardRequest(cardId, "user-FULL", "50000", "Thẻ thăm quan"), "staff-001")
        val issuedCard = cardRepo.findById(cardId)!!
        assertEquals("ACTIVE", issuedCard.status)
        assertEquals("user-FULL", issuedCard.userId)
        assertEquals(1, txRepo.store.count { it.type == "DEPOSIT_PAID" })

        // Bước 4: Khách quẹt thẻ tại cổng
        val tapResult = svc.processCardTap("CARD-FULL")
        assertTrue(tapResult.isSuccess)
        assertNotNull(cardRepo.findById(cardId)!!.lastUsedAt)

        // Bước 5: Khách trả thẻ
        svc.returnCard(cardId, "staff-001")
        val returnedCard = cardRepo.findById(cardId)!!
        assertEquals("AVAILABLE", returnedCard.status)
        assertNull(returnedCard.userId)
        assertEquals("NONE", returnedCard.depositStatus)
        assertEquals(1, txRepo.store.count { it.type == "DEPOSIT_REFUND" })

        // Tổng số giao dịch = 2 (DEPOSIT_PAID + DEPOSIT_REFUND)
        assertEquals(2, txRepo.store.size)
    }

    @Test
    fun `full lifecycle - phát hành → mất thẻ → bị khóa → cọc bị tịch thu`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val txRepo = FakeBalanceTransactionRepository()
        val svc = buildService(cardRepo, userRepo, txRepo)

        val cardId = svc.registerCard(RegisterCardRequest("CARD-LOST")).getOrThrow().cardId
        userRepo.addUser(makeTestUser("user-LOST"))
        svc.issueCard(IssueCardRequest(cardId, "user-LOST", "100000"), "staff-001")

        svc.blockCard(cardId, "Khách báo mất thẻ", "admin-001")

        val card = cardRepo.findById(cardId)!!
        assertEquals("BLOCKED", card.status)
        assertEquals("FORFEITED", card.depositStatus)

        val forfeit = txRepo.store.filter { it.type == "DEPOSIT_FORFEITED" }
        assertEquals(1, forfeit.size)
        assertEquals(BigDecimal("100000").negate(), forfeit[0].amount)
    }

    // ── 7. Lấy danh sách thẻ ─────────────────────────────────────────────────

    @Test
    fun `getAvailableCards - chỉ trả về thẻ AVAILABLE`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val svc = buildService(cardRepo, userRepo)

        svc.registerCard(RegisterCardRequest("CARD-AV1"))
        svc.registerCard(RegisterCardRequest("CARD-AV2"))
        val cardId3 = svc.registerCard(RegisterCardRequest("CARD-AV3")).getOrThrow().cardId
        userRepo.addUser(makeTestUser("user-LIST"))
        svc.issueCard(IssueCardRequest(cardId3, "user-LIST", "0"), "staff-001")

        val available = svc.getAvailableCards()
        assertEquals(2, available.size)
        assertTrue(available.all { it.status == "AVAILABLE" })
    }

    @Test
    fun `getMyCards - chỉ trả về thẻ của đúng user`() {
        val cardRepo = FakeCardRepository()
        val userRepo = FakeUserRepository()
        val svc = buildService(cardRepo, userRepo)

        userRepo.addUser(makeTestUser("user-A"))
        userRepo.addUser(makeTestUser("user-B"))

        val c1 = svc.registerCard(RegisterCardRequest("CARD-M1")).getOrThrow().cardId
        val c2 = svc.registerCard(RegisterCardRequest("CARD-M2")).getOrThrow().cardId
        val c3 = svc.registerCard(RegisterCardRequest("CARD-M3")).getOrThrow().cardId

        svc.issueCard(IssueCardRequest(c1, "user-A", "0"), "staff-001")
        svc.issueCard(IssueCardRequest(c2, "user-A", "0"), "staff-001")
        svc.issueCard(IssueCardRequest(c3, "user-B", "0"), "staff-001")

        val cardsA = svc.getMyCards("user-A")
        val cardsB = svc.getMyCards("user-B")

        assertEquals(2, cardsA.size)
        assertEquals(1, cardsB.size)
        assertTrue(cardsA.all { it.userId == "user-A" })
    }
}


