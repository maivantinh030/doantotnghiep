package com.park

import com.park.database.tables.BalanceTransactions
import com.park.database.tables.PaymentRecords
import com.park.database.tables.Users
import com.park.dto.MomoQrRequest
import com.park.services.MomoGatewayClient
import com.park.services.MomoGatewayCreateRequest
import com.park.services.MomoGatewayCreateResult
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MomoServiceTest {
    private lateinit var momoService: MomoService

    @BeforeTest
    fun setUp() {
        Database.connect(
            url = "jdbc:h2:mem:${UUID.randomUUID()};MODE=MySQL;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )

        transaction {
            SchemaUtils.create(Users, PaymentRecords, BalanceTransactions)
            val now = Instant.parse("2026-04-28T00:00:00Z")

            Users.insert {
                it[userId] = "user-1"
                it[accountId] = "account-1"
                it[fullName] = "Nguyen Van A"
                it[email] = "a@test.local"
                it[currentBalance] = BigDecimal("10000.00")
                it[avatarUrl] = null
                it[createdAt] = now
                it[updatedAt] = now
            }
        }

        momoService = MomoService(gatewayClient = FakeMomoGatewayClient())
    }

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(BalanceTransactions, PaymentRecords, Users)
        }
    }

    @Test
    fun `MoMo confirm credits wallet once`() {
        val createResult = momoService.createMomoQr(
            userId = "user-1",
            request = MomoQrRequest(
                amount = "50000",
                orderInfo = "Nap tien test"
            )
        )

        assertTrue(createResult.isSuccess)
        val qr = createResult.getOrThrow()

        transaction {
            val user = Users.selectAll().where { Users.userId eq "user-1" }.single()
            val payment = PaymentRecords.selectAll().where { PaymentRecords.paymentId eq qr.orderId }.single()

            assertMoneyEquals("10000", user[Users.currentBalance])
            assertEquals("PENDING", payment[PaymentRecords.status])
            assertEquals(0L, BalanceTransactions.selectAll().count())
        }

        val firstConfirm = momoService.confirmPayment("user-1", qr.orderId, "TRANS-1")
        val secondConfirm = momoService.confirmPayment("user-1", qr.orderId, "TRANS-1")

        assertTrue(firstConfirm.isSuccess)
        assertTrue(secondConfirm.isSuccess)
        assertEquals("SUCCESS", firstConfirm.getOrThrow().status)
        assertEquals("SUCCESS", secondConfirm.getOrThrow().status)

        transaction {
            val user = Users.selectAll().where { Users.userId eq "user-1" }.single()
            val payment = PaymentRecords.selectAll().where { PaymentRecords.paymentId eq qr.orderId }.single()
            val txs = BalanceTransactions.selectAll().toList()

            assertMoneyEquals("60000", user[Users.currentBalance])
            assertEquals("SUCCESS", payment[PaymentRecords.status])
            assertEquals(1, txs.size)
            assertEquals(qr.orderId, txs.single()[BalanceTransactions.referenceId])
            assertMoneyEquals("50000", txs.single()[BalanceTransactions.amount])
        }
    }

    private fun assertMoneyEquals(expected: String, actual: BigDecimal) {
        assertEquals(0, actual.compareTo(BigDecimal(expected)))
    }
}

private class FakeMomoGatewayClient : MomoGatewayClient {
    override fun createPayment(request: MomoGatewayCreateRequest): MomoGatewayCreateResult {
        return MomoGatewayCreateResult(
            orderId = request.orderId,
            requestId = request.requestId,
            amount = request.amount,
            resultCode = 0,
            message = "Thanh cong.",
            payUrl = "https://test-payment.momo.vn/v2/gateway/pay?t=${request.orderId}",
            deeplink = "momo://app?action=payWithApp&orderId=${request.orderId}",
            momoQrCodeUrl = "momo://app?action=payWithApp&isScanQR=true&orderId=${request.orderId}",
            responseTime = 1777334400000,
            rawResponse = "{}"
        )
    }
}
