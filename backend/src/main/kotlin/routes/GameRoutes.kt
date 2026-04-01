package com.park.routes

import com.park.dto.CreateGameRequest
import com.park.dto.UpdateGameRequest
import com.park.dto.UseGameRequest
import com.park.models.ErrorResponse
import com.park.services.GameService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.gameRoutes() {
    val gameService = GameService()

    route("/api/games") {

        // =====================================================
        // PUBLIC ENDPOINTS (không cần đăng nhập)
        // =====================================================

        /**
         * GET /api/games
         * Lấy danh sách game (có phân trang, tìm kiếm, lọc theo category)
         * Query params: page, size, category, search
         */
        get {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val category = call.request.queryParameters["category"]
                val search = call.request.queryParameters["search"]

                val result = gameService.getGames(page, size, category, search)

                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "success" to true,
                        "message" to "Lấy danh sách game thành công",
                        "data" to result
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(message = "Lỗi hệ thống: ${e.message}")
                )
            }
        }

        /**
         * GET /api/games/featured
         * Lấy danh sách game nổi bật
         * Query params: limit (default 10)
         */
        get("/featured") {
            try {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val games = gameService.getFeaturedGames(limit)

                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "success" to true,
                        "message" to "Lấy danh sách game nổi bật thành công",
                        "data" to games
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(message = "Lỗi hệ thống: ${e.message}")
                )
            }
        }

        /**
         * GET /api/games/categories
         * Lấy danh sách tất cả categories
         */
        get("/categories") {
            try {
                val categories = gameService.getCategories()

                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "success" to true,
                        "message" to "Lấy danh sách categories thành công",
                        "data" to categories
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(message = "Lỗi hệ thống: ${e.message}")
                )
            }
        }

        /**
         * GET /api/games/{gameId}
         * Lấy chi tiết một game
         */
        get("/{gameId}") {
            try {
                val gameId = call.parameters["gameId"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "Game ID không được để trống")
                    )

                val game = gameService.getGameById(gameId)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(message = "Không tìm thấy game")
                    )

                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "success" to true,
                        "message" to "Lấy chi tiết game thành công",
                        "data" to game
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(message = "Lỗi hệ thống: ${e.message}")
                )
            }
        }

        // =====================================================
        // TERMINAL ENDPOINTS (cần đăng nhập + role ADMIN)
        // =====================================================

        authenticate("auth-jwt") {

            /**
             * POST /api/games/{gameId}/play
             * Terminal quét NFC card → tìm vé hợp lệ → trừ 1 lượt chơi
             * Body: { "cardId": "..." }
             */
            post("/{gameId}/play") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()

                    if (role != "ADMIN") {
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ terminal (Admin) mới được gọi endpoint này")
                        )
                    }

                    val gameId = call.parameters["gameId"]
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Game ID không được để trống")
                        )

                    val request = call.receive<UseGameRequest>()
                    val result = gameService.useGame(gameId, request)

                    result.fold(
                        onSuccess = { response ->
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf(
                                    "success" to true,
                                    "message" to "Sử dụng game thành công",
                                    "data" to response
                                )
                            )
                        },
                        onFailure = { error ->
                            when (error) {
                                is NoSuchElementException -> call.respond(
                                    HttpStatusCode.NotFound,
                                    ErrorResponse(message = error.message ?: "Không tìm thấy")
                                )
                                is IllegalStateException -> call.respond(
                                    HttpStatusCode.Conflict,
                                    ErrorResponse(message = error.message ?: "Vé không hợp lệ")
                                )
                                else -> call.respond(
                                    HttpStatusCode.BadRequest,
                                    ErrorResponse(message = error.message ?: "Yêu cầu không hợp lệ")
                                )
                            }
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "Invalid request format: ${e.message}")
                    )
                }
            }
        }

        // =====================================================
        // ADMIN ENDPOINTS (cần đăng nhập + role ADMIN)
        // =====================================================

        authenticate("auth-jwt") {

            /**
             * POST /api/games
             * Tạo game mới (Admin only)
             */
            post {
                try {
                    // Kiểm tra quyền Admin
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()

                    if (role != "ADMIN") {
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Admin mới có quyền tạo game")
                        )
                    }

                    val request = call.receive<CreateGameRequest>()
                    val result = gameService.createGame(request)

                    result.fold(
                        onSuccess = { game ->
                            call.respond(
                                HttpStatusCode.Created,
                                mapOf(
                                    "success" to true,
                                    "message" to "Tạo game thành công",
                                    "data" to game
                                )
                            )
                        },
                        onFailure = { error ->
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(message = error.message ?: "Dữ liệu không hợp lệ")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "Invalid request format: ${e.message}")
                    )
                }
            }

            /**
             * PUT /api/games/{gameId}
             * Cập nhật game (Admin only)
             */
            put("/{gameId}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()

                    if (role != "ADMIN") {
                        return@put call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Admin mới có quyền cập nhật game")
                        )
                    }

                    val gameId = call.parameters["gameId"]
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Game ID không được để trống")
                        )

                    val request = call.receive<UpdateGameRequest>()
                    val result = gameService.updateGame(gameId, request)

                    result.fold(
                        onSuccess = { game ->
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf(
                                    "success" to true,
                                    "message" to "Cập nhật game thành công",
                                    "data" to game
                                )
                            )
                        },
                        onFailure = { error ->
                            when (error) {
                                is NoSuchElementException -> call.respond(
                                    HttpStatusCode.NotFound,
                                    ErrorResponse(message = error.message ?: "Game không tồn tại")
                                )
                                else -> call.respond(
                                    HttpStatusCode.BadRequest,
                                    ErrorResponse(message = error.message ?: "Dữ liệu không hợp lệ")
                                )
                            }
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "Invalid request format: ${e.message}")
                    )
                }
            }

            /**
             * DELETE /api/games/{gameId}
             * Xóa game (Admin only)
             */
            delete("/{gameId}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()

                    if (role != "ADMIN") {
                        return@delete call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Admin mới có quyền xóa game")
                        )
                    }

                    val gameId = call.parameters["gameId"]
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Game ID không được để trống")
                        )

                    val deleted = gameService.deleteGame(gameId)
                    if (deleted) {
                        call.respond(
                            HttpStatusCode.OK,
                            mapOf(
                                "success" to true,
                                "message" to "Xóa game thành công"
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(message = "Không tìm thấy game")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(message = "Lỗi hệ thống: ${e.message}")
                    )
                }
            }
        }
    }
}
