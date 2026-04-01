package com.park.routes

import com.park.dto.*
import com.park.models.ErrorResponse
import com.park.services.CardRequestService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.cardRequestRoutes() {
    val cardRequestService = CardRequestService()

    route("/api/card-requests") {
        authenticate("auth-jwt") {

            /**
             * POST /api/card-requests
             * User: gửi yêu cầu cấp thẻ qua app
             */
            post {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val dto = call.receive<CreateCardRequestDTO>()
                    val result = cardRequestService.createRequest(userId, dto)

                    result.fold(
                        onSuccess = { req ->
                            call.respond(HttpStatusCode.Created, mapOf("success" to true, "message" to "Yêu cầu đã được gửi", "data" to req))
                        },
                        onFailure = { e ->
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                }
            }

            /**
             * GET /api/card-requests/my
             * User: xem danh sách yêu cầu của mình
             */
            get("/my") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val requests = cardRequestService.getMyRequests(userId)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to requests))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = e.message ?: "Lỗi hệ thống"))
                }
            }

            /**
             * GET /api/card-requests?status=PENDING
             * Staff/Admin: xem danh sách yêu cầu theo trạng thái
             */
            get {
                try {
                    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Staff/Admin được thực hiện"))
                    }

                    val status = call.request.queryParameters["status"] ?: "PENDING"
                    val requests = cardRequestService.getRequestsByStatus(status)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to requests))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = e.message ?: "Lỗi hệ thống"))
                }
            }

            /**
             * POST /api/card-requests/{requestId}/review
             * Staff/Admin: duyệt hoặc từ chối yêu cầu
             */
            post("/{requestId}/review") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val adminId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Staff/Admin được thực hiện"))
                    }

                    val requestId = call.parameters["requestId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Thiếu requestId"))

                    val dto = call.receive<ApproveCardRequestDTO>()
                    val result = cardRequestService.reviewRequest(requestId, dto, adminId)

                    result.fold(
                        onSuccess = { req ->
                            val msg = if (dto.approved) "Duyệt yêu cầu thành công" else "Từ chối yêu cầu thành công"
                            call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to msg, "data" to req))
                        },
                        onFailure = { e ->
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                }
            }

            /**
             * POST /api/card-requests/{requestId}/complete
             * Staff: đánh dấu hoàn thành sau khi đã phát thẻ thực tế
             */
            post("/{requestId}/complete") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val adminId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Staff/Admin được thực hiện"))
                    }

                    val requestId = call.parameters["requestId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Thiếu requestId"))

                    val result = cardRequestService.completeRequest(requestId, adminId)

                    result.fold(
                        onSuccess = { req ->
                            call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Hoàn thành yêu cầu cấp thẻ", "data" to req))
                        },
                        onFailure = { e ->
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                }
            }

            /**
             * POST /api/card-requests/{requestId}/issue
             * Staff/Admin: ghi thẻ cho yêu cầu từ app và hoàn tất cấp thẻ
             */
            post("/{requestId}/issue") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val adminId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Staff/Admin được thực hiện"))
                    }

                    val requestId = call.parameters["requestId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Thiếu requestId"))

                    val dto = call.receive<IssueCardFromRequestDTO>()
                    val result = cardRequestService.issueCardForRequest(requestId, dto, adminId)

                    result.fold(
                        onSuccess = { req ->
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf("success" to true, "message" to "Cấp thẻ từ yêu cầu thành công", "data" to req)
                            )
                        },
                        onFailure = { e ->
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                }
            }
        }
    }
}
