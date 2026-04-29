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

                    val result = if (request.method == "MOMO") {
                        walletService.createMomoTopUp(userId, request)
                    } else {
                        walletService.topUp(userId, request)
                    }

                    result.fold(
                        onSuccess = { payment ->
                            call.respond(HttpStatusCode.Created, mapOf(
                                "success" to true,
                                "message" to if (request.method == "MOMO")
                                    "Tạo QR thành công, vui lòng quét mã để thanh toán"
                                else
                                    "Nạp tiền thành công",
                                "data" to payment
                            ))
                        },
                        onFailure = { error ->
                            when (error) {
                                is IllegalArgumentException -> call.respond(
                                    HttpStatusCode.BadRequest,
                                    ErrorResponse(message = error.message ?: "Yêu cầu không hợp lệ")
                                )
                                is NoSuchElementException -> call.respond(
                                    HttpStatusCode.NotFound,
                                    ErrorResponse(message = error.message ?: "Không tìm thấy")
                                )
                                else -> call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponse(message = error.message ?: "Lỗi hệ thống")
                                )
                            }
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid request: ${e.message}"))
                }
            }

            get("/topup/status/{orderId}") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val orderId = call.parameters["orderId"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Thiếu orderId"))

                    val payment = walletService.getPaymentByOrderId(userId, orderId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Không tìm thấy giao dịch"))

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Lấy trạng thái giao dịch thành công",
                        "data" to payment
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
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
        post("/momo/ipn") {
            try {
                val ipnBody = call.receiveText()
                println("=== MoMo IPN received ===\n$ipnBody")

                val success = walletService.handleMomoIpn(ipnBody)

                // MoMo yêu cầu luôn trả 200, dù success hay fail
                call.respond(HttpStatusCode.OK, mapOf(
                    "message" to if (success) "OK" else "Error",
                    "resultCode" to if (success) 0 else 99
                ))
            } catch (e: Exception) {
                println("=== MoMo IPN error: ${e.message} ===")
                call.respond(HttpStatusCode.OK, mapOf(
                    "message" to "Error",
                    "resultCode" to 99
                ))
            }
        }
    }
}
