package com.example.appcongvien.data.model

// ===== Responses =====
data class OrderDTO(
    val orderId: String,
    val userId: String,
    val subtotal: String,           // Tổng trước giảm giá
    val discountAmount: String,     // Số tiền được giảm
    val totalAmount: String,        // Số tiền thực trả (sau giảm giá)
    val voucherId: String?,
    val paymentMethod: String?,
    val status: String,             // "PENDING" | "COMPLETED" | "CANCELLED"
    val note: String?,
    val createdAt: String,
    val completedAt: String?,
    val cancelledAt: String?,
    val cancelledReason: String?,
    val items: List<OrderDetailDTO>? = null
)

data class OrderDetailDTO(
    val detailId: String,
    val gameId: String,
    val gameName: String? = null,
    val quantity: Int,
    val unitPrice: String,
    val lineTotal: String
)

data class TicketDTO(
    val ticketId: String,
    val userId: String,
    val gameId: String?,
    val purchaseOrderId: String?,
    val ticketType: String?,
    val remainingTurns: Int,
    val originalTurns: Int,
    val status: String,             // "VALID" | "USED" | "EXPIRED"
    val expiryDate: String?,
    val createdAt: String,
    val usedAt: String?
)

// ===== Requests =====
data class CreateOrderRequest(
    val items: List<OrderItemRequest>,
    val voucherCode: String? = null,
    val paymentMethod: String = "BALANCE",
    val note: String? = null
)

data class OrderItemRequest(
    val gameId: String,
    val quantity: Int
)

data class CancelOrderRequest(
    val reason: String? = null
)
