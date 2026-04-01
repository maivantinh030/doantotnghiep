package com.park.routes

import com.park.dto.RegisterPushTokenRequest
import com.park.dto.UnregisterPushTokenRequest
import com.park.models.ErrorResponse
import com.park.services.NotificationService
import com.park.services.PushTokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.notificationRoutes() {
    val notificationService = NotificationService()
    val pushTokenService = PushTokenService()

    route("/api/notifications") {
        authenticate("auth-jwt") {

            /**
             * GET /api/notifications
             * Danh sách thông báo
             */
            get {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val result = notificationService.getNotifications(userId, page, size)

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Lấy thông báo thành công",
                        "data" to result
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            post("/device-token") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val request = call.receive<RegisterPushTokenRequest>()
                    val registered = pushTokenService.registerToken(userId, request)

                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "success" to true,
                            "message" to "Push token registered",
                            "data" to registered
                        )
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Invalid push token request"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "System error: ${e.message}"))
                }
            }

            post("/device-token/unregister") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val request = call.receive<UnregisterPushTokenRequest>()
                    val unregistered = pushTokenService.unregisterToken(userId, request.token)

                    if (unregistered) {
                        call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Push token unregistered"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Push token not found"))
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Invalid push token request"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "System error: ${e.message}"))
                }
            }

            /**
             * GET /api/notifications/unread-count
             * Số thông báo chưa đọc
             */
            get("/unread-count") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val count = notificationService.getUnreadCount(userId)
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "data" to mapOf("unreadCount" to count)
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/notifications/{id}/read
             * Đánh dấu đã đọc
             */
            post("/{id}/read") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val id = call.parameters["id"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Notification ID không được để trống"))

                    val success = notificationService.markAsRead(id, userId)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Đánh dấu đã đọc"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Không tìm thấy thông báo"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/notifications/read-all
             * Đọc tất cả
             */
            post("/read-all") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    notificationService.markAllAsRead(userId)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Đã đọc tất cả thông báo"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * DELETE /api/notifications/{id}
             * Xóa thông báo
             */
            delete("/{id}") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@delete call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val id = call.parameters["id"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Notification ID không được để trống"))

                    val deleted = notificationService.deleteNotification(id)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Xóa thông báo thành công"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Không tìm thấy thông báo"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }
        }
    }
}
