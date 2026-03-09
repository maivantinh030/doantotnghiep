package com.park.repositories

import com.park.database.tables.BookingOrderDetails
import com.park.database.tables.BookingOrders
import com.park.database.tables.Tickets
import com.park.entities.BookingOrder
import com.park.entities.BookingOrderDetail
import com.park.entities.Ticket
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface IOrderRepository {
    fun createOrder(order: BookingOrder): BookingOrder
    fun createOrderDetail(detail: BookingOrderDetail): BookingOrderDetail
    fun createTicket(ticket: Ticket): Ticket
    fun findOrderById(orderId: String): BookingOrder?
    fun findOrdersByUserId(userId: String, limit: Int, offset: Long): List<BookingOrder>
    fun findOrderDetailsByOrderId(orderId: String): List<BookingOrderDetail>
    fun findTicketsByUserId(userId: String, limit: Int, offset: Long): List<Ticket>
    fun findTicketById(ticketId: String): Ticket?
    fun findValidTicketByUserAndGame(userId: String, gameId: String): Ticket?
    fun countOrdersByUserId(userId: String): Long
    fun countTicketsByUserId(userId: String): Long
    fun updateOrderStatus(orderId: String, status: String, cancelledReason: String? = null): Boolean
    fun updateTicketStatus(ticketId: String, status: String): Boolean
    fun decrementTicketTurns(ticketId: String): Boolean
}

class OrderRepository : IOrderRepository {

    override fun createOrder(order: BookingOrder): BookingOrder {
        return transaction {
            BookingOrders.insert {
                it[orderId] = order.orderId
                it[userId] = order.userId
                it[subtotal] = order.subtotal
                it[discountAmount] = order.discountAmount
                it[totalAmount] = order.totalAmount
                it[voucherId] = order.voucherId
                it[paymentMethod] = order.paymentMethod
                it[status] = order.status
                it[note] = order.note
                it[createdAt] = order.createdAt
            }
            order
        }
    }

    override fun createOrderDetail(detail: BookingOrderDetail): BookingOrderDetail {
        return transaction {
            BookingOrderDetails.insert {
                it[detailId] = detail.detailId
                it[orderId] = detail.orderId
                it[gameId] = detail.gameId
                it[quantity] = detail.quantity
                it[unitPrice] = detail.unitPrice
                it[lineTotal] = detail.lineTotal
            }
            detail
        }
    }

    override fun createTicket(ticket: Ticket): Ticket {
        return transaction {
            Tickets.insert {
                it[ticketId] = ticket.ticketId
                it[userId] = ticket.userId
                it[gameId] = ticket.gameId
                it[purchaseOrderId] = ticket.purchaseOrderId
                it[ticketType] = ticket.ticketType
                it[remainingTurns] = ticket.remainingTurns
                it[originalTurns] = ticket.originalTurns
                it[status] = ticket.status
                it[expiryDate] = ticket.expiryDate
                it[createdAt] = ticket.createdAt
            }
            ticket
        }
    }

    override fun findOrderById(orderId: String): BookingOrder? {
        return transaction {
            BookingOrders.selectAll().where { BookingOrders.orderId eq orderId }
                .singleOrNull()?.let { mapOrder(it) }
        }
    }

    override fun findOrdersByUserId(userId: String, limit: Int, offset: Long): List<BookingOrder> {
        return transaction {
            BookingOrders.selectAll().where { BookingOrders.userId eq userId }
                .orderBy(BookingOrders.createdAt, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { mapOrder(it) }
        }
    }

    override fun findOrderDetailsByOrderId(orderId: String): List<BookingOrderDetail> {
        return transaction {
            BookingOrderDetails.selectAll().where { BookingOrderDetails.orderId eq orderId }
                .map { mapOrderDetail(it) }
        }
    }

    override fun findTicketsByUserId(userId: String, limit: Int, offset: Long): List<Ticket> {
        return transaction {
            Tickets.selectAll().where { Tickets.userId eq userId }
                .orderBy(Tickets.createdAt, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { mapTicket(it) }
        }
    }

    override fun findTicketById(ticketId: String): Ticket? {
        return transaction {
            Tickets.selectAll().where { Tickets.ticketId eq ticketId }
                .singleOrNull()?.let { mapTicket(it) }
        }
    }

    override fun findValidTicketByUserAndGame(userId: String, gameId: String): Ticket? {
        val now = Instant.now()
        return transaction {
            Tickets.selectAll()
                .where {
                    (Tickets.userId eq userId) and
                    (Tickets.gameId eq gameId) and
                    (Tickets.status eq "VALID") and
                    (Tickets.remainingTurns greater 0) and
                    ((Tickets.expiryDate.isNull()) or (Tickets.expiryDate greaterEq now))
                }
                .orderBy(Tickets.expiryDate, SortOrder.ASC_NULLS_LAST)
                .limit(1)
                .singleOrNull()?.let { mapTicket(it) }
        }
    }

    override fun countOrdersByUserId(userId: String): Long {
        return transaction {
            BookingOrders.selectAll().where { BookingOrders.userId eq userId }.count()
        }
    }

    override fun countTicketsByUserId(userId: String): Long {
        return transaction {
            Tickets.selectAll().where { Tickets.userId eq userId }.count()
        }
    }

    override fun updateOrderStatus(orderId: String, status: String, cancelledReason: String?): Boolean {
        return transaction {
            BookingOrders.update(where = { BookingOrders.orderId eq orderId }) {
                it[BookingOrders.status] = status
                when (status) {
                    "COMPLETED" -> it[completedAt] = Instant.now()
                    "CANCELLED" -> {
                        it[cancelledAt] = Instant.now()
                        if (cancelledReason != null) it[BookingOrders.cancelledReason] = cancelledReason
                    }
                }
            } > 0
        }
    }

    override fun updateTicketStatus(ticketId: String, status: String): Boolean {
        return transaction {
            Tickets.update(where = { Tickets.ticketId eq ticketId }) {
                it[Tickets.status] = status
                if (status == "USED") it[usedAt] = Instant.now()
            } > 0
        }
    }

    override fun decrementTicketTurns(ticketId: String): Boolean {
        return transaction {
            Tickets.update(where = { Tickets.ticketId eq ticketId }) {
                with(SqlExpressionBuilder) {
                    it[remainingTurns] = remainingTurns - 1
                }
            } > 0
        }
    }

    private fun mapOrder(row: ResultRow): BookingOrder {
        return BookingOrder(
            orderId = row[BookingOrders.orderId],
            userId = row[BookingOrders.userId],
            subtotal = row[BookingOrders.subtotal],
            discountAmount = row[BookingOrders.discountAmount],
            totalAmount = row[BookingOrders.totalAmount],
            voucherId = row[BookingOrders.voucherId],
            paymentMethod = row[BookingOrders.paymentMethod],
            status = row[BookingOrders.status],
            note = row[BookingOrders.note],
            createdAt = row[BookingOrders.createdAt],
            completedAt = row[BookingOrders.completedAt],
            cancelledAt = row[BookingOrders.cancelledAt],
            cancelledReason = row[BookingOrders.cancelledReason]
        )
    }

    private fun mapOrderDetail(row: ResultRow): BookingOrderDetail {
        return BookingOrderDetail(
            detailId = row[BookingOrderDetails.detailId],
            orderId = row[BookingOrderDetails.orderId],
            gameId = row[BookingOrderDetails.gameId],
            quantity = row[BookingOrderDetails.quantity],
            unitPrice = row[BookingOrderDetails.unitPrice],
            lineTotal = row[BookingOrderDetails.lineTotal]
        )
    }

    private fun mapTicket(row: ResultRow): Ticket {
        return Ticket(
            ticketId = row[Tickets.ticketId],
            userId = row[Tickets.userId],
            gameId = row[Tickets.gameId],
            purchaseOrderId = row[Tickets.purchaseOrderId],
            ticketType = row[Tickets.ticketType],
            remainingTurns = row[Tickets.remainingTurns],
            originalTurns = row[Tickets.originalTurns],
            status = row[Tickets.status],
            expiryDate = row[Tickets.expiryDate],
            createdAt = row[Tickets.createdAt],
            usedAt = row[Tickets.usedAt]
        )
    }
}
