package com.park.routes

import com.park.dto.TopUpRequest
import com.park.models.ErrorResponse
import com.park.services.WalletService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.walletRoutes() {
    val walletService = WalletService()

    route("/api/wallet") {
        authenticate("auth-jwt") {

            /**
             * GET /api/wallet/balance
             * Xem số dư ví
             */
            get("/balance") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val balance = walletService.getBalance(userId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "User không tồn tại"))

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Lấy số dư thành công",
                        "data" to balance
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * GET /api/wallet/transactions
             * Lịch sử giao dịch
             * Query params: page, size, type (TOPUP, PAYMENT, REFUND, BONUS, ADJUSTMENT)
             */
            get("/transactions") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                    val type = call.request.queryParameters["type"]
                    val result = walletService.getTransactions(userId, page, size, type)

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Lấy lịch sử giao dịch thành công",
                        "data" to result
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/wallet/topup
             * Nạp tiền
             */
            post("/topup") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val request = call.receive<TopUpRequest>()
                    val result = walletService.topUp(userId, request)

                    result.fold(
                        onSuccess = { payment ->
                            call.respond(HttpStatusCode.Created, mapOf(
                                "success" to true,
                                "message" to "Nạp tiền thành công",
                                "data" to payment
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

            /**
             * GET /api/wallet/payments
             * Lịch sử thanh toán (nạp tiền)
             */
            get("/payments") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                    val result = walletService.getPaymentHistory(userId, page, size)

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Lấy lịch sử thanh toán thành công",
                        "data" to result
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }
        }
    }
}
