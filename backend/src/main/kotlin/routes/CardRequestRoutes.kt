package com.park.routes

import com.park.dto.ApproveCardRequestDTO
import com.park.dto.CreateCardRequestDTO
import com.park.dto.IssueCardFromRequestDTO
import com.park.models.ErrorResponse
import com.park.services.CardRequestService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.cardRequestRoutes() {
    val cardRequestService = CardRequestService()

    route("/api/card-requests") {
        authenticate("auth-jwt") {

            post {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse(message = "Invalid token")
                        )

                    val dto = call.receive<CreateCardRequestDTO>()
                    val result = cardRequestService.createRequest(userId, dto)

                    result.fold(
                        onSuccess = { req ->
                            call.respond(
                                HttpStatusCode.Created,
                                mapOf("success" to true, "message" to "Yêu cầu đã được gửi", "data" to req)
                            )
                        },
                        onFailure = { e ->
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(message = e.message ?: "Lỗi")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                }
            }

            get("/my") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse(message = "Invalid token")
                        )

                    val requests = cardRequestService.getMyRequests(userId)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to requests))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(message = e.message ?: "Lỗi hệ thống")
                    )
                }
            }

            get {
                try {
                    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@get call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Staff/Admin được thực hiện")
                        )
                    }

                    val status = call.request.queryParameters["status"] ?: "PENDING"
                    val requests = cardRequestService.getRequestsByStatus(status)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to requests))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(message = e.message ?: "Lỗi hệ thống")
                    )
                }
            }

            post("/{requestId}/cancel") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse(message = "Invalid token")
                        )

                    val requestId = call.parameters["requestId"]
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Thiếu requestId")
                        )

                    val result = cardRequestService.cancelRequest(requestId, userId)

                    result.fold(
                        onSuccess = { req ->
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf("success" to true, "message" to "Hủy yêu cầu thành công", "data" to req)
                            )
                        },
                        onFailure = { e ->
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(message = e.message ?: "Lỗi")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                }
            }

            post("/{requestId}/review") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val adminId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse(message = "Invalid token")
                        )

                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Staff/Admin được thực hiện")
                        )
                    }

                    val requestId = call.parameters["requestId"]
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Thiếu requestId")
                        )

                    val dto = call.receive<ApproveCardRequestDTO>()
                    val result = cardRequestService.reviewRequest(requestId, dto, adminId)

                    result.fold(
                        onSuccess = { req ->
                            val msg = if (dto.approved) {
                                "Hoàn thành yêu cầu thành công"
                            } else {
                                "Từ chối yêu cầu thành công"
                            }
                            call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to msg, "data" to req))
                        },
                        onFailure = { e ->
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(message = e.message ?: "Lỗi")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                }
            }

            post("/{requestId}/complete") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val adminId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse(message = "Invalid token")
                        )

                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Staff/Admin được thực hiện")
                        )
                    }

                    val requestId = call.parameters["requestId"]
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Thiếu requestId")
                        )

                    val result = cardRequestService.completeRequest(requestId, adminId)

                    result.fold(
                        onSuccess = { req ->
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf("success" to true, "message" to "Hoàn thành yêu cầu cấp thẻ", "data" to req)
                            )
                        },
                        onFailure = { e ->
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(message = e.message ?: "Lỗi")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                }
            }

            post("/{requestId}/issue") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val adminId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse(message = "Invalid token")
                        )

                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Staff/Admin được thực hiện")
                        )
                    }

                    val requestId = call.parameters["requestId"]
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Thiếu requestId")
                        )

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
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(message = e.message ?: "Lỗi")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                }
            }
        }
    }
}
