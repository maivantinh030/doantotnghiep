package com.park.dto

import com.park.entities.BookingOrder
import com.park.entities.BookingOrderDetail
import com.park.entities.Ticket
import kotlinx.serialization.Serializable

@Serializable
data class OrderDTO(
    val orderId: String,
    val userId: String,
    val subtotal: String,
    val discountAmount: String,
    val totalAmount: String,
    val voucherId: String?,
    val paymentMethod: String?,
    val status: String,
    val note: String?,
    val createdAt: String,
    val completedAt: String?,
    val cancelledAt: String?,
    val cancelledReason: String?,
    val items: List<OrderDetailDTO>? = null
) {
    companion object {
        fun fromEntity(order: BookingOrder, items: List<BookingOrderDetail>? = null): OrderDTO {
            return OrderDTO(
                orderId = order.orderId,
                userId = order.userId,
                subtotal = order.subtotal.toString(),
                discountAmount = order.discountAmount.toString(),
                totalAmount = order.totalAmount.toString(),
                voucherId = order.voucherId,
                paymentMethod = order.paymentMethod,
                status = order.status,
                note = order.note,
                createdAt = order.createdAt.toString(),
                completedAt = order.completedAt?.toString(),
                cancelledAt = order.cancelledAt?.toString(),
                cancelledReason = order.cancelledReason,
                items = items?.map { OrderDetailDTO.fromEntity(it) }
            )
        }

        fun fromEntityWithDetails(order: BookingOrder, details: List<OrderDetailDTO>): OrderDTO {
            return OrderDTO(
                orderId = order.orderId,
                userId = order.userId,
                subtotal = order.subtotal.toString(),
                discountAmount = order.discountAmount.toString(),
                totalAmount = order.totalAmount.toString(),
                voucherId = order.voucherId,
                paymentMethod = order.paymentMethod,
                status = order.status,
                note = order.note,
                createdAt = order.createdAt.toString(),
                completedAt = order.completedAt?.toString(),
                cancelledAt = order.cancelledAt?.toString(),
                cancelledReason = order.cancelledReason,
                items = details
            )
        }
    }
}

@Serializable
data class OrderDetailDTO(
    val detailId: String,
    val gameId: String,
    val gameName: String? = null,
    val quantity: Int,
    val unitPrice: String,
    val lineTotal: String
) {
    companion object {
        fun fromEntity(detail: BookingOrderDetail, gameName: String? = null): OrderDetailDTO {
            return OrderDetailDTO(
                detailId = detail.detailId,
                gameId = detail.gameId,
                gameName = gameName,
                quantity = detail.quantity,
                unitPrice = detail.unitPrice.toString(),
                lineTotal = detail.lineTotal.toString()
            )
        }
    }
}

@Serializable
data class TicketDTO(
    val ticketId: String,
    val userId: String,
    val gameId: String?,
    val purchaseOrderId: String?,
    val ticketType: String?,
    val remainingTurns: Int,
    val originalTurns: Int,
    val status: String,
    val expiryDate: String?,
    val createdAt: String,
    val usedAt: String?
) {
    companion object {
        fun fromEntity(ticket: Ticket): TicketDTO {
            return TicketDTO(
                ticketId = ticket.ticketId,
                userId = ticket.userId,
                gameId = ticket.gameId,
                purchaseOrderId = ticket.purchaseOrderId,
                ticketType = ticket.ticketType,
                remainingTurns = ticket.remainingTurns,
                originalTurns = ticket.originalTurns,
                status = ticket.status,
                expiryDate = ticket.expiryDate?.toString(),
                createdAt = ticket.createdAt.toString(),
                usedAt = ticket.usedAt?.toString()
            )
        }
    }
}

@Serializable
data class CreateOrderRequest(
    val items: List<OrderItemRequest>,
    val voucherCode: String? = null,
    val paymentMethod: String = "BALANCE",
    val note: String? = null
)

@Serializable
data class OrderItemRequest(
    val gameId: String,
    val quantity: Int = 1
)

@Serializable
data class CancelOrderRequest(
    val reason: String? = null
)
