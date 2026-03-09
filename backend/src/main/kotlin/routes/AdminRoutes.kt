package com.park.routes

import com.park.dto.*
import com.park.models.ErrorResponse
import com.park.services.AdminService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private fun ApplicationCall.requireAdminId(): String? {
    val principal = principal<JWTPrincipal>()
    val role = principal?.payload?.getClaim("role")?.asString()
    return if (role == "ADMIN") principal?.payload?.getClaim("userId")?.asString() else null
}

fun Route.adminRoutes() {
    val adminService = AdminService()

    route("/api/admin") {

        // ─── Public Auth Routes ───────────────────────────────────────────

        /**
         * POST /api/admin/auth/register
         * Tạo tài khoản admin mới
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
         * Đăng nhập admin
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

        // ─── Protected Admin Routes ───────────────────────────────────────

        authenticate("auth-jwt") {

            /**
             * GET /api/admin/dashboard/stats
             * Thống kê tổng quan
             */
            get("/dashboard/stats") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val stats = adminService.getDashboardStats()
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to stats))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * GET /api/admin/revenue/chart?period=daily|weekly|monthly
             * Biểu đồ doanh thu theo kỳ
             */
            get("/revenue/chart") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val period = call.request.queryParameters["period"] ?: "daily"
                    val chartData = adminService.getRevenueChart(period)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to chartData))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            // ─── User Management ──────────────────────────────────────────

            /**
             * GET /api/admin/users?page=1&size=20
             * Danh sách tất cả người dùng
             */
            get("/users") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val result = adminService.getAllUsers(page, size)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to result))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/admin/users/{userId}/lock
             * Khóa tài khoản người dùng
             */
            post("/users/{userId}/lock") {
                try {
                    call.requireAdminId() ?: return@post call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val userId = call.parameters["userId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "userId không được để trống"))
                    val success = adminService.lockUser(userId)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Đã khóa tài khoản"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Người dùng không tồn tại"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/admin/users/{userId}/unlock
             * Mở khóa tài khoản người dùng
             */
            post("/users/{userId}/unlock") {
                try {
                    call.requireAdminId() ?: return@post call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val userId = call.parameters["userId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "userId không được để trống"))
                    val success = adminService.unlockUser(userId)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Đã mở khóa tài khoản"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Người dùng không tồn tại"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/admin/users/{userId}/adjust-balance
             * Điều chỉnh số dư ví
             */
            post("/users/{userId}/adjust-balance") {
                try {
                    val adminId = call.requireAdminId() ?: return@post call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val userId = call.parameters["userId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "userId không được để trống"))
                    val request = call.receive<AdjustBalanceRequest>()
                    adminService.adjustBalance(userId, request, adminId).fold(
                        onSuccess = { call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to it)) },
                        onFailure = { call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = it.message ?: "Lỗi")) }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * PUT /api/admin/users/{userId}/membership
             * Cập nhật hạng thành viên
             */
            put("/users/{userId}/membership") {
                try {
                    call.requireAdminId() ?: return@put call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val userId = call.parameters["userId"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "userId không được để trống"))
                    val request = call.receive<UpdateMembershipRequest>()
                    val success = adminService.updateMembership(userId, request)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Cập nhật hạng thành viên thành công"))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Hạng thành viên không hợp lệ hoặc người dùng không tồn tại"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            // ─── Orders ───────────────────────────────────────────────────

            /**
             * GET /api/admin/orders?page=1&size=20
             * Danh sách tất cả đơn hàng
             */
            get("/orders") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val result = adminService.getAllOrders(page, size)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to result))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            // ─── Transactions ─────────────────────────────────────────────

            /**
             * GET /api/admin/transactions?page=1&size=20
             * Danh sách tất cả giao dịch
             */
            get("/transactions") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val result = adminService.getAllTransactions(page, size)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to result))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            // ─── Notifications ────────────────────────────────────────────

            /**
             * POST /api/admin/notifications/send
             * Gửi thông báo broadcast
             */
            post("/notifications/send") {
                try {
                    val adminId = call.requireAdminId() ?: return@post call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val request = call.receive<SendNotificationRequest>()
                    if (request.title.isBlank() || request.message.isBlank()) {
                        return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Tiêu đề và nội dung không được để trống"))
                    }
                    adminService.sendBroadcastNotification(adminId, request).fold(
                        onSuccess = { call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to it)) },
                        onFailure = { call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = it.message ?: "Lỗi")) }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * GET /api/admin/notifications?page=1&size=20
             * Lịch sử thông báo đã gửi
             */
            get("/notifications") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val result = adminService.getBroadcastHistory(page, size)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to result))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            // ─── Support ──────────────────────────────────────────────────

            /**
             * GET /api/admin/support/messages
             * Tất cả tin nhắn hỗ trợ
             */
            get("/support/messages") {
                try {
                    call.requireAdminId() ?: return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val result = adminService.getAllSupportMessages()
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to result))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/admin/support/reply
             * Phản hồi tin nhắn hỗ trợ
             */
            post("/support/reply") {
                try {
                    val adminId = call.requireAdminId() ?: return@post call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val request = call.receive<AdminReplyRequest>()
                    adminService.replyToUser(request, adminId).fold(
                        onSuccess = { call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to it)) },
                        onFailure = { call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = it.message ?: "Lỗi")) }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }
        }
    }
}
