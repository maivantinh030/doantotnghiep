package com.park.database.tables

import org.jetbrains.exposed.sql.Table

object BookingOrderDetails : Table("booking_order_details") {
    val detailId = varchar("detail_id", 36)
    val orderId = varchar("order_id", 36)
    val gameId = varchar("game_id", 36)
    val quantity = integer("quantity")
    val unitPrice = decimal("unit_price", 10, 2)
    val lineTotal = decimal("line_total", 15, 2)

    override val primaryKey = PrimaryKey(detailId)
}
