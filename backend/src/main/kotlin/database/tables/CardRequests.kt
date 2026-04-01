package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal
import java.time.Instant

// Yêu cầu cấp thẻ từ app trước khi đến quầy (Case B)
object CardRequests : Table("card_requests") {
    val requestId = varchar("request_id", 36)
    val userId = varchar("user_id", 36)
    val status = varchar("status", 20).default("PENDING") // PENDING | APPROVED | REJECTED | COMPLETED
    val depositPaidOnline = bool("deposit_paid_online").default(false)
    val depositAmount = decimal("deposit_amount", 15, 2).default(BigDecimal.ZERO)
    val note = text("note").nullable()
    val approvedBy = varchar("approved_by", 36).nullable() // admin_id
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(requestId)
}
