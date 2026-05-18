package com.park.routes

import com.park.dto.BlockCardRequest
import com.park.dto.CardLookupRequest
import com.park.dto.IssueCardRequest
import com.park.dto.RegisterCardRequest
import com.park.models.ErrorResponse
import com.park.services.CardService
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

fun Route.cardRoutes() {
    val cardService = CardService()

    route("/api/cards") {
        authenticate("auth-jwt") {

            get {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse(message = "Invalid token")
                        )

                    val cards = cardService.getMyCards(userId)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to cards))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(message = e.message ?: "Lỗi hệ thống")
                    )
                }
            }

            get("/{cardId}") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse(message = "Invalid token")
                        )

                    val cardId = call.parameters["cardId"]
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Thiếu mã thẻ")
                        )

                    val card = cardService.getCardById(cardId, userId)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(message = "Không tìm thấy thẻ")
                        )

                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to card))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(message = e.message ?: "Lỗi hệ thống")
                    )
                }
            }

            post("/register") {
                try {
                    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Staff/Admin được thực hiện")
                        )
                    }

                    val request = call.receive<RegisterCardRequest>()
                    val result = cardService.registerCard(request)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(
                                HttpStatusCode.Created,
                                mapOf("success" to true, "message" to "Đăng ký thẻ thành công", "data" to card)
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

            post("/issue") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val staffId = principal?.payload?.getClaim("userId")?.asString()
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

                    val request = call.receive<IssueCardRequest>()
                    val result = cardService.issueCard(request, staffId)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf("success" to true, "message" to "Phát hành thẻ thành công", "data" to card)
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

            post("/{cardId}/return") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val staffId = principal?.payload?.getClaim("userId")?.asString()
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

                    val cardId = call.parameters["cardId"]
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Thiếu mã thẻ")
                        )

                    val result = cardService.returnCard(cardId, staffId)

                    result.fold(
                        onSuccess = { summary ->
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf("success" to true, "message" to "Trả thẻ thành công", "data" to summary)
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
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(message = e.message ?: "Lỗi hệ thống")
                    )
                }
            }

            post("/{cardId}/block") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val actorId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse(message = "Invalid token")
                        )

                    if (role !in listOf("USER", "STAFF", "ADMIN")) {
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Bạn không có quyền thực hiện")
                        )
                    }

                    val cardId = call.parameters["cardId"]
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Thiếu mã thẻ")
                        )

                    if (role == "USER" && cardService.getCardById(cardId, actorId) == null) {
                        return@post call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(message = "Không tìm thấy thẻ của bạn")
                        )
                    }

                    val request = call.receive<BlockCardRequest>()
                    val result = cardService.blockCard(cardId, request.reason, actorId)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf("success" to true, "message" to "Khóa thẻ thành công", "data" to card)
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

            get("/available") {
                try {
                    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@get call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Staff/Admin được thực hiện")
                        )
                    }

                    val cards = cardService.getAvailableCards()
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to cards))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(message = e.message ?: "Lỗi hệ thống")
                    )
                }
            }

            post("/tap") {
                try {
                    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Terminal/Staff được thực hiện")
                        )
                    }

                    val request = call.receive<CardLookupRequest>()
                    val result = cardService.processCardTap(request.cardId)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to card))
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
