package com.park.routes

import com.park.dto.SendMessageRequest
import com.park.models.ErrorResponse
import com.park.services.SupportService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.supportRoutes() {
    val supportService = SupportService()

    route("/api/support") {
        authenticate("auth-jwt") {

            /**
             * GET /api/support/messages
             * Lịch sử chat hỗ trợ
             */
            get("/messages") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 50
                    val result = supportService.getChatHistory(userId, page, size)

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Lấy lịch sử chat thành công",
                        "data" to result
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/support/messages
             * Gửi tin nhắn hỗ trợ
             */
            post("/messages") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val request = call.receive<SendMessageRequest>()
                    val result = supportService.sendMessage(userId, request)

                    result.fold(
                        onSuccess = { message ->
                            call.respond(HttpStatusCode.Created, mapOf(
                                "success" to true,
                                "message" to "Gửi tin nhắn thành công",
                                "data" to message
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
    }
}
