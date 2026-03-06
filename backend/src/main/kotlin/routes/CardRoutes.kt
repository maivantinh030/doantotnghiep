package com.park.routes

import com.park.dto.BlockCardRequest
import com.park.dto.LinkCardRequest
import com.park.dto.UpdateCardRequest
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
             * Danh sách thẻ của tôi
             */
            get {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val cards = cardService.getMyCards(userId)
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Lấy danh sách thẻ thành công",
                        "data" to cards
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * GET /api/cards/{cardId}
             * Chi tiết thẻ
             */
            get("/{cardId}") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val cardId = call.parameters["cardId"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Card ID không được để trống"))

                    val card = cardService.getCardById(cardId, userId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Không tìm thấy thẻ"))

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Lấy chi tiết thẻ thành công",
                        "data" to card
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/cards/link
             * Liên kết thẻ vật lý
             */
            post("/link") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val request = call.receive<LinkCardRequest>()
                    val result = cardService.linkCard(userId, request)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(HttpStatusCode.Created, mapOf(
                                "success" to true,
                                "message" to "Liên kết thẻ thành công",
                                "data" to card
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
             * PUT /api/cards/{cardId}
             * Cập nhật thẻ
             */
            put("/{cardId}") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@put call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val cardId = call.parameters["cardId"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Card ID không được để trống"))

                    val request = call.receive<UpdateCardRequest>()
                    val result = cardService.updateCard(cardId, userId, request)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(HttpStatusCode.OK, mapOf(
                                "success" to true,
                                "message" to "Cập nhật thẻ thành công",
                                "data" to card
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
             * POST /api/cards/{cardId}/block
             * Khóa thẻ
             */
            post("/{cardId}/block") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val cardId = call.parameters["cardId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Card ID không được để trống"))

                    val request = call.receive<BlockCardRequest>()
                    val result = cardService.blockCard(cardId, userId, request.reason)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(HttpStatusCode.OK, mapOf(
                                "success" to true,
                                "message" to "Khóa thẻ thành công",
                                "data" to card
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
             * POST /api/cards/{cardId}/unblock
             * Mở khóa thẻ
             */
            post("/{cardId}/unblock") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val cardId = call.parameters["cardId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Card ID không được để trống"))

                    val result = cardService.unblockCard(cardId, userId)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(HttpStatusCode.OK, mapOf(
                                "success" to true,
                                "message" to "Mở khóa thẻ thành công",
                                "data" to card
                            ))
                        },
                        onFailure = { error ->
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = error.message ?: "Lỗi"))
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/cards/virtual
             * Tạo thẻ ảo HCE không cần thẻ vật lý (virtual-first)
             */
            post("/virtual") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val result = cardService.createVirtualOnlyCard(userId)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(HttpStatusCode.Created, mapOf(
                                "success" to true,
                                "message" to "Tạo thẻ ảo thành công",
                                "data" to card
                            ))
                        },
                        onFailure = { error ->
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = error.message ?: "Lỗi"))
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/cards/{cardId}/virtual
             * Tạo thẻ ảo HCE cho thẻ vật lý
             */
            post("/{cardId}/virtual") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val cardId = call.parameters["cardId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Card ID không được để trống"))

                    val result = cardService.generateVirtualCard(cardId, userId)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(HttpStatusCode.OK, mapOf(
                                "success" to true,
                                "message" to "Tạo thẻ ảo thành công",
                                "data" to card
                            ))
                        },
                        onFailure = { error ->
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = error.message ?: "Lỗi"))
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * DELETE /api/cards/{cardId}/virtual
             * Xóa thẻ ảo HCE
             */
            delete("/{cardId}/virtual") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@delete call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val cardId = call.parameters["cardId"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Card ID không được để trống"))

                    val result = cardService.removeVirtualCard(cardId, userId)

                    result.fold(
                        onSuccess = { card ->
                            call.respond(HttpStatusCode.OK, mapOf(
                                "success" to true,
                                "message" to "Xóa thẻ ảo thành công",
                                "data" to card
                            ))
                        },
                        onFailure = { error ->
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = error.message ?: "Lỗi"))
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * DELETE /api/cards/{cardId}/unlink
             * Hủy liên kết thẻ
             */
            delete("/{cardId}/unlink") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@delete call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val cardId = call.parameters["cardId"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Card ID không được để trống"))

                    val success = cardService.unlinkCard(cardId, userId)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Hủy liên kết thẻ thành công"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Không tìm thấy thẻ"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }
        }
    }
}
