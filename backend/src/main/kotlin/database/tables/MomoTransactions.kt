package com.park.database.tables
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object MomoTransactions: Table("momotransactions") {
    val orderId      = varchar("order_id", 50)
    val userId       = varchar("user_id", 36)
    val amount       = decimal("amount", 15, 2)
    val status       = varchar("status", 20)   // pending/success/failed
    val description  = varchar("description", 255)
    val momoTransId  = varchar("momo_trans_id", 50).nullable()  // transId từ IPN
    val qrData       = text("qr_data").nullable()
    val createdAt    = timestamp("created_at").default(Instant.now())
    val completedAt  = timestamp("completed_at").nullable()

    override val primaryKey = PrimaryKey(orderId)
}