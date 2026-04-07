package com.park.routes

import com.park.dto.*
import com.park.models.ErrorResponse
import com.park.services.AdminService
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

private fun ApplicationCall.requireAdminId(): String? {
    val principal = principal<JWTPrincipal>()
    val role = principal?.payload?.getClaim("role")?.asString()
    return if (role == "ADMIN") principal?.payload?.getClaim("userId")?.asString() else null
}

fun Route.adminRoutes() {
    val adminService = AdminService()

    route("/api/admin") {

        // â”€â”€â”€ Public Auth Routes â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

        /**
         * POST /api/admin/auth/register
         * Táº¡o tĂ i khoáº£n admin má»›i
         */
        post("/auth/register") {
            try {
                val request = call.receive<CreateAdminRequest>()
                val response = adminService.registerAdmin(request)
                if (response.success) {
                    call.respond(HttpStatusCode.Created, response)
                } else {
                    call.respond(HttpStatusCode.BadRequest, response)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid request: ${e.message}"))
            }
        }

        /**
         * POST /api/admin/auth/login
         * ÄÄƒng nháº­p admin
         */
        post("/auth/login") {
            try {
                val request = call.receive<AdminLoginRequest>()
                val response = adminService.loginAdmin(request)
                if (response.success) {
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, response)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid request: ${e.message}"))
            }
        }

        // â”€â”€â”€ Protected Admin Routes â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

        authenticate("auth-jwt") {

            /**
             * GET /api/admin/dashboard/stats
             * Thá»‘ng kĂª tá»•ng quan
             */
            get("/dashboard/stats") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "YĂªu cáº§u quyá»n admin")
                    )
                    val stats = adminService.getDashboardStats()
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to stats))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lá»—i há»‡ thá»‘ng: ${e.message}"))
                }
            }

            /**
             * GET /api/admin/revenue/chart?period=daily|weekly|monthly
             * Biá»ƒu Ä‘á»“ doanh thu theo ká»³
             */
            get("/revenue/chart") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "YĂªu cáº§u quyá»n admin")
                    )
                    val period = call.request.queryParameters["period"] ?: "daily"
                    val chartData = adminService.getRevenueChart(period)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to chartData))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lá»—i há»‡ thá»‘ng: ${e.message}"))
                }
            }

            // â”€â”€â”€ User Management â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

            /**
             * GET /api/admin/users?page=1&size=20
             * Danh sĂ¡ch táº¥t cáº£ ngÆ°á»i dĂ¹ng
             */
            get("/users") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "YĂªu cáº§u quyá»n admin")
                    )
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val result = adminService.getAllUsers(page, size)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to result))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lá»—i há»‡ thá»‘ng: ${e.message}"))
                }
            }

            /**
             * POST /api/admin/users/{userId}/lock
             * KhĂ³a tĂ i khoáº£n ngÆ°á»i dĂ¹ng
             */
            post("/users/{userId}/lock") {
                try {
                    call.requireAdminId() ?: return@post call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "YĂªu cáº§u quyá»n admin")
                    )
                    val userId = call.parameters["userId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "userId khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"))
                    val success = adminService.lockUser(userId)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "ÄĂ£ khĂ³a tĂ i khoáº£n"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "NgÆ°á»i dĂ¹ng khĂ´ng tá»“n táº¡i"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lá»—i há»‡ thá»‘ng: ${e.message}"))
                }
            }

            /**
             * POST /api/admin/users/{userId}/unlock
             * Má»Ÿ khĂ³a tĂ i khoáº£n ngÆ°á»i dĂ¹ng
             */
            post("/users/{userId}/unlock") {
                try {
                    call.requireAdminId() ?: return@post call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "YĂªu cáº§u quyá»n admin")
                    )
                    val userId = call.parameters["userId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "userId khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"))
                    val success = adminService.unlockUser(userId)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "ÄĂ£ má»Ÿ khĂ³a tĂ i khoáº£n"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "NgÆ°á»i dĂ¹ng khĂ´ng tá»“n táº¡i"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lá»—i há»‡ thá»‘ng: ${e.message}"))
                }
            }

            /**
             * POST /api/admin/users/{userId}/adjust-balance
             * Äiá»u chá»‰nh sá»‘ dÆ° vĂ­
             */
            post("/users/{userId}/adjust-balance") {
                try {
                    val adminId = call.requireAdminId() ?: return@post call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "YĂªu cáº§u quyá»n admin")
                    )
                    val userId = call.parameters["userId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "userId khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"))
                    val request = call.receive<AdjustBalanceRequest>()
                    adminService.adjustBalance(userId, request, adminId).fold(
                        onSuccess = { call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to it)) },
                        onFailure = { call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = it.message ?: "Lá»—i")) }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lá»—i há»‡ thá»‘ng: ${e.message}"))
                }
            }

            // â”€â”€â”€ Transactions â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

            /**
             * GET /api/admin/transactions?page=1&size=20
             * Danh sĂ¡ch táº¥t cáº£ giao dá»‹ch
             */
            get("/transactions") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "YĂªu cáº§u quyá»n admin")
                    )
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val result = adminService.getAllTransactions(page, size)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to result))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lá»—i há»‡ thá»‘ng: ${e.message}"))
                }
            }

            // â”€â”€â”€ Notifications â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

            /**
             * POST /api/admin/notifications/send
             * Gá»­i thĂ´ng bĂ¡o broadcast
             */
            post("/notifications/send") {
                try {
                    val adminId = call.requireAdminId() ?: return@post call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "YĂªu cáº§u quyá»n admin")
                    )
                    val request = call.receive<SendNotificationRequest>()
                    if (request.title.isBlank() || request.message.isBlank()) {
                        return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "TiĂªu Ä‘á» vĂ  ná»™i dung khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng"))
                    }
                    adminService.sendBroadcastNotification(adminId, request).fold(
                        onSuccess = { call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to it)) },
                        onFailure = { call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = it.message ?: "Lá»—i")) }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lá»—i há»‡ thá»‘ng: ${e.message}"))
                }
            }

            /**
             * GET /api/admin/notifications?page=1&size=20
             * Lá»‹ch sá»­ thĂ´ng bĂ¡o Ä‘Ă£ gá»­i
             */
            get("/notifications") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "YĂªu cáº§u quyá»n admin")
                    )
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val result = adminService.getBroadcastHistory(page, size)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to result))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lá»—i há»‡ thá»‘ng: ${e.message}"))
                }
            }

            // â”€â”€â”€ Support â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

            // Statistics endpoints
            /**
             * GET /api/admin/statistics/filters
             * Metadata bo loc cho man hinh statistics
             */
            get("/statistics/filters") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yeu cau quyen admin")
                    )
                    val filters = adminService.getStatisticsFilters()
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to filters))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Loi he thong: ${e.message}"))
                }
            }

            /**
             * GET /api/admin/statistics/trend
             * Trend doanh thu va nguoi choi
             */
            get("/statistics/trend") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yeu cau quyen admin")
                    )
                    val period = call.request.queryParameters["period"] ?: "daily"
                    val startDate = call.request.queryParameters["startDate"]
                    val endDate = call.request.queryParameters["endDate"]
                    val game = call.request.queryParameters["game"]
                    val area = call.request.queryParameters["area"]
                    val status = call.request.queryParameters["status"]
                    val trend = adminService.getStatisticsTrend(
                        period = period,
                        startDate = startDate,
                        endDate = endDate,
                        game = game,
                        area = area,
                        status = status
                    )
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to trend))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Invalid query parameters"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Loi he thong: ${e.message}"))
                }
            }

            /**
             * GET /api/admin/statistics/games
             * Thong ke theo tung tro choi + top/bottom insights
             */
            get("/statistics/games") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yeu cau quyen admin")
                    )
                    val startDate = call.request.queryParameters["startDate"]
                    val endDate = call.request.queryParameters["endDate"]
                    val game = call.request.queryParameters["game"]
                    val area = call.request.queryParameters["area"]
                    val status = call.request.queryParameters["status"]
                    val search = call.request.queryParameters["search"]
                    val result = adminService.getStatisticsGames(
                        startDate = startDate,
                        endDate = endDate,
                        game = game,
                        area = area,
                        status = status,
                        search = search
                    )
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to result))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Invalid query parameters"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Loi he thong: ${e.message}"))
                }
            }

            /**
             * GET /api/admin/statistics/table?page=1&size=10
             * Du lieu bang chi tiet co phan trang
             */
            get("/statistics/table") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yeu cau quyen admin")
                    )
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                    val startDate = call.request.queryParameters["startDate"]
                    val endDate = call.request.queryParameters["endDate"]
                    val game = call.request.queryParameters["game"]
                    val area = call.request.queryParameters["area"]
                    val status = call.request.queryParameters["status"]
                    val search = call.request.queryParameters["search"]
                    val result = adminService.getStatisticsTable(
                        page = page,
                        size = size,
                        startDate = startDate,
                        endDate = endDate,
                        game = game,
                        area = area,
                        status = status,
                        search = search
                    )
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to result))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Invalid query parameters"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Loi he thong: ${e.message}"))
                }
            }

            get("/support/messages") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "YĂªu cáº§u quyá»n admin")
                    )
                    val result = adminService.getAllSupportMessages()
                    call.respond(HttpStatusCode.OK, AdminSupportApiResponse(
                        success = true,
                        message = "Láº¥y danh sĂ¡ch tin nháº¯n há»— trá»£ thĂ nh cĂ´ng",
                        data = result
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lá»—i há»‡ thá»‘ng: ${e.message}"))
                }
            }

            /**
             * POST /api/admin/support/reply
             * Pháº£n há»“i tin nháº¯n há»— trá»£
             */
            post("/support/reply") {
                try {
                    val adminId = call.requireAdminId() ?: return@post call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "YĂªu cáº§u quyá»n admin")
                    )
                    val request = call.receive<AdminReplyRequest>()
                    adminService.replyToUser(request, adminId).fold(
                        onSuccess = { msg ->
                            // Gá»­i realtime tá»›i user Ä‘ang káº¿t ná»‘i
                            val wsMsg = Json.encodeToString(WsSupportMessage(
                                messageId = msg.messageId,
                                userId = msg.userId,
                                content = msg.content,
                                senderType = "ADMIN",
                                createdAt = msg.createdAt.toString()
                            ))
                            SupportWebSocketManager.sendToUser(request.userId, wsMsg)
                            call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to msg))
                        },
                        onFailure = { call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = it.message ?: "Lá»—i")) }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lá»—i há»‡ thá»‘ng: ${e.message}"))
                }
            }
        }
    }
}

