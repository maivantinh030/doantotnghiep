package com.park.routes

import com.park.dto.CancelOrderRequest
import com.park.dto.CreateOrderRequest
import com.park.models.ErrorResponse
import com.park.services.OrderService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.orderRoutes() {
    val orderService = OrderService()

    authenticate("auth-jwt") {

        route("/api/orders") {

            /**
             * POST /api/orders
             * Tạo đơn hàng mới (mua vé)
             */
            post {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val request = call.receive<CreateOrderRequest>()
                    val result = orderService.createOrder(userId, request)

                    result.fold(
                        onSuccess = { order ->
                            call.respond(HttpStatusCode.Created, mapOf(
                                "success" to true,
                                "message" to "Tạo đơn hàng thành công",
                                "data" to order
                            ))
                        },
                        onFailure = { error ->
                            when (error) {
                                is NoSuchElementException -> call.respond(HttpStatusCode.NotFound, ErrorResponse(message = error.message ?: "Không tìm thấy"))
                                else -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = error.message ?: "Lỗi"))
                            }
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid request: ${e.message}"))
                }
            }

            /**
             * GET /api/orders
             * Lịch sử đơn hàng
             */
            get {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                    val result = orderService.getMyOrders(userId, page, size)

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Lấy lịch sử đơn hàng thành công",
                        "data" to result
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * GET /api/orders/{orderId}
             * Chi tiết đơn hàng
             */
            get("/{orderId}") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val orderId = call.parameters["orderId"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Order ID không được để trống"))

                    val order = orderService.getOrderById(orderId, userId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Không tìm thấy đơn hàng"))

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Lấy chi tiết đơn hàng thành công",
                        "data" to order
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/orders/{orderId}/cancel
             * Hủy đơn hàng
             */
            post("/{orderId}/cancel") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val orderId = call.parameters["orderId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Order ID không được để trống"))

                    val request = call.receive<CancelOrderRequest>()
                    val result = orderService.cancelOrder(orderId, userId, request.reason)

                    result.fold(
                        onSuccess = { order ->
                            call.respond(HttpStatusCode.OK, mapOf(
                                "success" to true,
                                "message" to "Hủy đơn hàng thành công",
                                "data" to order
                            ))
                        },
                        onFailure = { error ->
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = error.message ?: "Lỗi"))
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid request: ${e.message}"))
                }
            }
        }

        /**
         * GET /api/tickets
         * Vé của tôi
         */
        route("/api/tickets") {
            get {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                    val result = orderService.getMyTickets(userId, page, size)

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Lấy danh sách vé thành công",
                        "data" to result
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }
        }
    }
}
