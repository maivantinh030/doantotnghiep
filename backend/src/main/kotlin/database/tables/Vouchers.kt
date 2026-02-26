package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Vouchers : Table("vouchers") {
    val voucherId = varchar("voucher_id", 36)
    val code = varchar("code", 50).uniqueIndex()
    val title = varchar("title", 100)
    val description = text("description").nullable()
    val discountType = varchar("discount_type", 20)
    val discountValue = decimal("discount_value", 10, 2)
    val maxDiscount = decimal("max_discount", 10, 2).nullable()
    val minOrderValue = decimal("min_order_value", 10, 2).default(java.math.BigDecimal.ZERO)
    val usageLimit = integer("usage_limit").nullable()
    val usedCount = integer("used_count").default(0)
    val perUserLimit = integer("per_user_limit").default(1)
    val applicableGames = text("applicable_games").nullable() // JSON array of game_ids
    val startDate = timestamp("start_date")
    val endDate = timestamp("end_date")
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(voucherId)
}
