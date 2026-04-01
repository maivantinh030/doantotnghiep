package com.park.routes

import com.park.dto.CreateReviewRequest
import com.park.dto.UpdateReviewRequest
import com.park.models.ErrorResponse
import com.park.services.GameReviewService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.gameReviewRoutes() {
    val reviewService = GameReviewService()

    /**
     * GET /api/games/{gameId}/reviews
     * Đánh giá của game (public)
     */
    route("/api/games/{gameId}/reviews") {
        get {
            try {
                val gameId = call.parameters["gameId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Game ID không được để trống"))

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val result = reviewService.getReviewsByGameId(gameId, page, size)

                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "message" to "Lấy đánh giá thành công",
                    "data" to result
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
            }
        }
    }

    route("/api/reviews") {
        authenticate("auth-jwt") {

            /**
             * GET /api/reviews/my-review?gameId={gameId}
             * Lấy đánh giá của user hiện tại cho game
             */
            get("/my-review") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val gameId = call.request.queryParameters["gameId"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "gameId không được để trống"))

                    val review = reviewService.getMyReview(userId, gameId)
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "OK",
                        "data" to review
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /**
             * POST /api/reviews
             * Viết đánh giá
             */
            post {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val request = call.receive<CreateReviewRequest>()
                    val result = reviewService.createReview(userId, request)

                    result.fold(
                        onSuccess = { review ->
                            call.respond(HttpStatusCode.Created, mapOf(
                                "success" to true,
                                "message" to "Đánh giá thành công",
                                "data" to review
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
             * PUT /api/reviews/{reviewId}
             * Sửa đánh giá
             */
            put("/{reviewId}") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@put call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val reviewId = call.parameters["reviewId"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Review ID không được để trống"))

                    val request = call.receive<UpdateReviewRequest>()
                    val result = reviewService.updateReview(reviewId, userId, request)

                    result.fold(
                        onSuccess = { review ->
                            call.respond(HttpStatusCode.OK, mapOf(
                                "success" to true,
                                "message" to "Cập nhật đánh giá thành công",
                                "data" to review
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
             * DELETE /api/reviews/{reviewId}
             * Xóa đánh giá
             */
            delete("/{reviewId}") {
                try {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userId")?.asString()
                        ?: return@delete call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid token"))

                    val reviewId = call.parameters["reviewId"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Review ID không được để trống"))

                    val deleted = reviewService.deleteReview(reviewId, userId)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, mapOf("success" to true, "message" to "Xóa đánh giá thành công"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Không tìm thấy đánh giá"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }
        }
    }
}
