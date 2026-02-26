package com.park.services

import com.park.dto.*
import com.park.entities.*
import com.park.repositories.*
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class OrderService(
    private val orderRepository: IOrderRepository = OrderRepository(),
    private val gameRepository: IGameRepository = GameRepository(),
    private val userRepository: IUserRepository = UserRepository(),
    private val voucherRepository: IVoucherRepository = VoucherRepository(),
    private val userVoucherRepository: IUserVoucherRepository = UserVoucherRepository(),
    private val balanceTransactionRepository: IBalanceTransactionRepository = BalanceTransactionRepository()
) {

    fun createOrder(userId: String, request: CreateOrderRequest): Result<OrderDTO> {
        if (request.items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Đơn hàng phải có ít nhất 1 sản phẩm"))
        }

        // Tính toán subtotal
        var subtotal = BigDecimal.ZERO
        val orderDetails = mutableListOf<BookingOrderDetail>()
        val orderId = UUID.randomUUID().toString()

        for (item in request.items) {
            val game = gameRepository.findById(item.gameId)
                ?: return Result.failure(NoSuchElementException("Game ${item.gameId} không tồn tại"))

            if (game.status != "ACTIVE") {
                return Result.failure(IllegalStateException("Game '${game.name}' hiện không hoạt động"))
            }

            val lineTotal = game.pricePerTurn.multiply(BigDecimal(item.quantity))
            subtotal = subtotal.add(lineTotal)

            orderDetails.add(
                BookingOrderDetail(
                    detailId = UUID.randomUUID().toString(),
                    orderId = orderId,
                    gameId = item.gameId,
                    quantity = item.quantity,
                    unitPrice = game.pricePerTurn,
                    lineTotal = lineTotal
                )
            )
        }

        // Áp dụng voucher nếu có
        var discountAmount = BigDecimal.ZERO
        var voucherId: String? = null

        if (!request.voucherCode.isNullOrBlank()) {
            val voucher = voucherRepository.findByCode(request.voucherCode)
                ?: return Result.failure(NoSuchElementException("Mã voucher không hợp lệ"))

            val now = Instant.now()
            if (!voucher.isActive || now.isBefore(voucher.startDate) || now.isAfter(voucher.endDate)) {
                return Result.failure(IllegalStateException("Voucher không còn hiệu lực"))
            }

            if (subtotal < voucher.minOrderValue) {
                return Result.failure(IllegalStateException("Đơn hàng chưa đạt giá trị tối thiểu ${voucher.minOrderValue}"))
            }

            discountAmount = when (voucher.discountType) {
                "PERCENT" -> {
                    val discount = subtotal.multiply(voucher.discountValue).divide(BigDecimal(100))
                    if (voucher.maxDiscount != null && discount > voucher.maxDiscount) {
                        voucher.maxDiscount
                    } else discount
                }
                "FIXED_AMOUNT" -> voucher.discountValue
                else -> BigDecimal.ZERO
            }
            voucherId = voucher.voucherId
        }

        val totalAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO)

        // Kiểm tra số dư nếu thanh toán bằng BALANCE
        if (request.paymentMethod == "BALANCE") {
            val user = userRepository.findById(userId)
                ?: return Result.failure(NoSuchElementException("User không tồn tại"))

            if (user.currentBalance < totalAmount) {
                return Result.failure(IllegalStateException("Số dư không đủ. Cần ${totalAmount}, hiện có ${user.currentBalance}"))
            }

            // Trừ tiền
            val newBalance = user.currentBalance.subtract(totalAmount)
            userRepository.update(userId, mapOf("currentBalance" to newBalance))

            // Ghi log giao dịch
            balanceTransactionRepository.create(
                BalanceTransaction(
                    transactionId = UUID.randomUUID().toString(),
                    userId = userId,
                    amount = totalAmount.negate(),
                    balanceBefore = user.currentBalance,
                    balanceAfter = newBalance,
                    type = "PAYMENT",
                    referenceType = "ORDER",
                    referenceId = orderId,
                    description = "Thanh toán đơn hàng #${orderId.take(8)}",
                    createdAt = Instant.now(),
                    createdBy = null
                )
            )
        }

        val now = Instant.now()
        val order = BookingOrder(
            orderId = orderId,
            userId = userId,
            subtotal = subtotal,
            discountAmount = discountAmount,
            totalAmount = totalAmount,
            voucherId = voucherId,
            paymentMethod = request.paymentMethod,
            status = if (request.paymentMethod == "BALANCE") "COMPLETED" else "PENDING",
            note = request.note,
            createdAt = now,
            completedAt = if (request.paymentMethod == "BALANCE") now else null,
            cancelledAt = null,
            cancelledReason = null
        )

        orderRepository.createOrder(order)
        for (detail in orderDetails) {
            orderRepository.createOrderDetail(detail)
        }

        // Tạo tickets
        if (request.paymentMethod == "BALANCE") {
            createTicketsForOrder(userId, orderId, orderDetails)
        }

        // Đánh dấu voucher đã dùng
        if (voucherId != null) {
            val uv = userVoucherRepository.findByUserAndVoucher(userId, voucherId)
            if (uv != null) {
                userVoucherRepository.markUsed(uv.id, orderId)
            }
            voucherRepository.incrementUsedCount(voucherId)
        }

        val savedDetails = orderRepository.findOrderDetailsByOrderId(orderId)
        return Result.success(OrderDTO.fromEntity(order, savedDetails))
    }

    fun getMyOrders(userId: String, page: Int, size: Int): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()
        val orders = orderRepository.findOrdersByUserId(userId, size, offset)
        val total = orderRepository.countOrdersByUserId(userId)

        val dtos = orders.map { order ->
            val details = orderRepository.findOrderDetailsByOrderId(order.orderId)
            OrderDTO.fromEntity(order, details)
        }

        return mapOf(
            "items" to dtos,
            "total" to total,
            "page" to page,
            "size" to size
        )
    }

    fun getOrderById(orderId: String, userId: String): OrderDTO? {
        val order = orderRepository.findOrderById(orderId) ?: return null
        if (order.userId != userId) return null
        val details = orderRepository.findOrderDetailsByOrderId(orderId)
        return OrderDTO.fromEntity(order, details)
    }

    fun cancelOrder(orderId: String, userId: String, reason: String?): Result<OrderDTO> {
        val order = orderRepository.findOrderById(orderId)
            ?: return Result.failure(NoSuchElementException("Đơn hàng không tồn tại"))

        if (order.userId != userId) {
            return Result.failure(IllegalAccessException("Không có quyền hủy đơn hàng này"))
        }

        if (order.status != "PENDING") {
            return Result.failure(IllegalStateException("Chỉ có thể hủy đơn hàng ở trạng thái PENDING"))
        }

        orderRepository.updateOrderStatus(orderId, "CANCELLED", reason)

        val updated = orderRepository.findOrderById(orderId)!!
        val details = orderRepository.findOrderDetailsByOrderId(orderId)
        return Result.success(OrderDTO.fromEntity(updated, details))
    }

    fun getMyTickets(userId: String, page: Int, size: Int): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()
        val tickets = orderRepository.findTicketsByUserId(userId, size, offset)
        val total = orderRepository.countTicketsByUserId(userId)

        return mapOf(
            "tickets" to tickets.map { TicketDTO.fromEntity(it) },
            "total" to total,
            "page" to page,
            "size" to size
        )
    }

    private fun createTicketsForOrder(userId: String, orderId: String, details: List<BookingOrderDetail>) {
        val now = Instant.now()
        val expiryDate = now.plus(30, ChronoUnit.DAYS)

        for (detail in details) {
            orderRepository.createTicket(
                Ticket(
                    ticketId = UUID.randomUUID().toString(),
                    userId = userId,
                    gameId = detail.gameId,
                    purchaseOrderId = orderId,
                    ticketType = "SINGLE",
                    remainingTurns = detail.quantity,
                    originalTurns = detail.quantity,
                    status = "VALID",
                    expiryDate = expiryDate,
                    createdAt = now,
                    usedAt = null
                )
            )
        }
    }
}
