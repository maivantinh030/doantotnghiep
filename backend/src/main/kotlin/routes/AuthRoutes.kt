package com.park.routes

import com.park.models.ErrorResponse
import com.park.models.LoginRequest
import com.park.models.RegisterRequest
import com.park.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    val authService = AuthService()

    route("/api/auth") {
        /**
         * POST /api/auth/register
         * Đăng ký tài khoản mới
         */
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val response = authService.register(request)

                if (response.success) {
                    call.respond(HttpStatusCode.Created, response)
                } else {
                    call.respond(HttpStatusCode.BadRequest, response)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        success = false,
                        message = "Invalid request format: ${e.message}"
                    )
                )
            }
        }

        /**
         * POST /api/auth/login
         * Đăng nhập
         */
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val response = authService.login(request)

                if (response.success) {
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, response)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        success = false,
                        message = "Invalid request format: ${e.message}"
                    )
                )
            }
        }

        /**
         * GET /api/auth/health
         * Kiểm tra trạng thái API
         */
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "status" to "OK",
                    "service" to "Park Adventure Auth API",
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }
}
