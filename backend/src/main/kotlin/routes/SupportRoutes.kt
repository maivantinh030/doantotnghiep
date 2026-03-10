package com.park.routes

import com.park.dto.SendMessageRequest
import com.park.dto.SupportHistoryApiResponse
import com.park.dto.WsSupportMessage
import com.park.models.ErrorResponse
import com.park.services.SupportService
import com.park.websocket.SupportWebSocketManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
                    call.respond(HttpStatusCode.OK, SupportHistoryApiResponse(
                        success = true,
                        message = "Lấy lịch sử chat thành công",
                        data = result
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/support/messages
             * Gửi tin nhắn hỗ trợ + broadcast realtime tới admin
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
                            // Broadcast tới tất cả admin đang kết nối
                            val wsMsg = Json.encodeToString(WsSupportMessage(
                                messageId = message.messageId,
                                userId = message.userId,
                                content = message.content,
                                senderType = message.senderType,
                                createdAt = message.createdAt
                            ))
                            SupportWebSocketManager.broadcastToAdmins(wsMsg)

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
