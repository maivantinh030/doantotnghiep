package com.park.routes

import com.park.websocket.SupportWebSocketManager
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Route.supportWebSocketRoutes() {
    authenticate("auth-jwt") {

        /**
         * WS /ws/support
         * User kết nối để nhận phản hồi từ admin realtime
         */
        webSocket("/ws/support") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()

            if (userId == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized"))
                return@webSocket
            }

            SupportWebSocketManager.addUserSession(userId, this)
            try {
                for (frame in incoming) {
                    // User chỉ nhận, không gửi qua WebSocket
                }
            } catch (e: Exception) {
                // Connection closed or error
            } finally {
                SupportWebSocketManager.removeUserSession(userId)
            }
        }

        /**
         * WS /ws/admin/support
         * Admin kết nối để nhận tin nhắn mới từ users realtime
         */
        webSocket("/ws/admin/support") {
            val principal = call.principal<JWTPrincipal>()
            val adminId = principal?.payload?.getClaim("userId")?.asString()
            val role = principal?.payload?.getClaim("role")?.asString()

            if (adminId == null || role != "ADMIN") {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Forbidden"))
                return@webSocket
            }

            SupportWebSocketManager.addAdminSession(adminId, this)
            try {
                for (frame in incoming) {
                    // Admin chỉ nhận, không gửi qua WebSocket
                }
            } catch (e: Exception) {
                // Connection closed or error
            } finally {
                SupportWebSocketManager.removeAdminSession(adminId)
            }
        }
    }
}
