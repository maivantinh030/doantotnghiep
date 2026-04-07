package com.park.routes

import com.park.dto.*
import com.park.models.ErrorResponse
import com.park.services.CardService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.cardRoutes() {
    val cardService = CardService()

    route("/api/cards") {
        authenticate("auth-jwt") {

            /**
             * GET /api/cards
             * User: danh sách thẻ của tôi
             */
            get {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val cards = cardService.getMyCards(userId)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to cards))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = e.message ?: "Lỗi hệ thống"))
                }
            }

            /**
             * GET /api/cards/{cardId}
             * User: chi tiết thẻ của mình
             */
            get("/{cardId}") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val cardId = call.parameters["cardId"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Thiếu cardId"))

                    val card = cardService.getCardById(cardId, userId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Không tìm thấy thẻ"))

                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to card))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = e.message ?: "Lỗi hệ thống"))
                }
            }

            /**
             * POST /api/cards/register
             * Staff: đăng ký thẻ trắng vào hệ thống (chưa liên kết với ai)
             */
            post("/register") {
                try {
                    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Staff/Admin được thực hiện"))
                    }

                    val request = call.receive<RegisterCardRequest>()
                    val result = cardService.registerCard(request)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(HttpStatusCode.Created, mapOf("success" to true, "message" to "Đăng ký thẻ thành công", "data" to card))
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
             * POST /api/cards/issue
             * Staff: phát hành thẻ cho khách (liên kết thẻ với tài khoản + ghi nhận tiền cọc)
             */
            post("/issue") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val staffId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Staff/Admin được thực hiện"))
                    }

                    val request = call.receive<IssueCardRequest>()
                    val result = cardService.issueCard(request, staffId)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Phát hành thẻ thành công", "data" to card))
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
             * POST /api/cards/{cardId}/return
             * Staff: xử lý trả thẻ (unlink + hoàn cọc + hoàn balance)
             */
            post("/{cardId}/return") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val staffId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Staff/Admin được thực hiện"))
                    }

                    val cardId = call.parameters["cardId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Thiếu cardId"))

                    val result = cardService.returnCard(cardId, staffId)

                    result.fold(
                        onSuccess = { summary ->
                            call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Trả thẻ thành công", "data" to summary))
                        },
                        onFailure = { e ->
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = e.message ?: "Lỗi"))
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = e.message ?: "Lỗi hệ thống"))
                }
            }

            /**
             * POST /api/cards/{cardId}/block
             * Staff/Admin: khóa thẻ mất (không cần thẻ vật lý)
             */
            post("/{cardId}/block") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val staffId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Staff/Admin được thực hiện"))
                    }

                    val cardId = call.parameters["cardId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Thiếu cardId"))

                    val request = call.receive<BlockCardRequest>()
                    val result = cardService.blockCard(cardId, request.reason, staffId)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Khóa thẻ thành công", "data" to card))
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
             * GET /api/cards/available
             * Staff/Admin: danh sách thẻ chưa liên kết (để chọn phát hành)
             */
            get("/available") {
                try {
                    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Staff/Admin được thực hiện"))
                    }
                    val cards = cardService.getAvailableCards()
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to cards))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = e.message ?: "Lỗi hệ thống"))
                }
            }

            /**
             * POST /api/cards/tap
             * Terminal: xử lý quẹt thẻ — kiểm tra trạng thái thẻ
             */
            post("/tap") {
                try {
                    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse(message = "Chỉ Terminal/Staff được thực hiện"))
                    }

                    val request = call.receive<CardLookupRequest>()
                    val result = cardService.processCardTap(request.cardId)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to card))
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
