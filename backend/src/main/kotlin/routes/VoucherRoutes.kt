package com.park.routes

import com.park.dto.CreateVoucherRequest
import com.park.dto.UpdateVoucherRequest
import com.park.models.ErrorResponse
import com.park.services.VoucherService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.voucherRoutes() {
    val voucherService = VoucherService()

    route("/api/vouchers") {

        // === PUBLIC ===

        /**
         * GET /api/vouchers
         * Danh sách voucher đang có hiệu lực
         */
        get {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val result = voucherService.getAvailableVouchers(page, size)

                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "message" to "Lấy danh sách voucher thành công",
                    "data" to result
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
            }
        }

        /**
         * GET /api/vouchers/code/{code}
         * Tìm voucher theo mã
         */
        get("/code/{code}") {
            try {
                val code = call.parameters["code"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Mã voucher không được để trống"))

                val voucher = voucherService.getVoucherByCode(code)
                    ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Không tìm thấy voucher"))

                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "message" to "Tìm voucher thành công",
                    "data" to voucher
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
            }
        }

        // === USER (cần đăng nhập) ===

        authenticate("auth-jwt") {

            /**
             * POST /api/vouchers/{voucherId}/claim
             * Nhận voucher vào ví
             */
            post("/{voucherId}/claim") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val voucherId = call.parameters["voucherId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Voucher ID không được để trống"))

                    val result = voucherService.claimVoucher(userId, voucherId)

                    result.fold(
                        onSuccess = { uv ->
                            call.respond(HttpStatusCode.Created, mapOf(
                                "success" to true,
                                "message" to "Nhận voucher thành công",
                                "data" to uv
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
             * GET /api/vouchers/my-vouchers
             * Voucher của tôi
             */
            get("/my-vouchers") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                    val result = voucherService.getMyVouchers(userId, page, size)

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Lấy voucher của tôi thành công",
                        "data" to result
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            // === ADMIN ===

            /**
             * POST /api/vouchers
             * Tạo voucher mới (Admin only)
             */
            post {
                try {
                    val role = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("role")?.asString()
                    if (role != "ADMIN") {
                        return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Admin mới có quyền tạo voucher"))
                    }

                    val request = call.receive<CreateVoucherRequest>()
                    val result = voucherService.createVoucher(request)

                    result.fold(
                        onSuccess = { voucher ->
                            call.respond(HttpStatusCode.Created, mapOf(
                                "success" to true,
                                "message" to "Tạo voucher thành công",
                                "data" to voucher
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
             * PUT /api/vouchers/{voucherId}
             * Cập nhật voucher (Admin only)
             */
            put("/{voucherId}") {
                try {
                    val role = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("role")?.asString()
                    if (role != "ADMIN") {
                        return@put call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Admin mới có quyền cập nhật voucher"))
                    }

                    val voucherId = call.parameters["voucherId"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Voucher ID không được để trống"))

                    val request = call.receive<UpdateVoucherRequest>()
                    val result = voucherService.updateVoucher(voucherId, request)

                    result.fold(
                        onSuccess = { voucher ->
                            call.respond(HttpStatusCode.OK, mapOf(
                                "success" to true,
                                "message" to "Cập nhật voucher thành công",
                                "data" to voucher
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
             * DELETE /api/vouchers/{voucherId}
             * Xóa voucher (Admin only)
             */
            delete("/{voucherId}") {
                try {
                    val role = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("role")?.asString()
                    if (role != "ADMIN") {
                        return@delete call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Admin mới có quyền xóa voucher"))
                    }

                    val voucherId = call.parameters["voucherId"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Voucher ID không được để trống"))

                    val deleted = voucherService.deleteVoucher(voucherId)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Xóa voucher thành công"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Không tìm thấy voucher"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }
        }
    }
}
