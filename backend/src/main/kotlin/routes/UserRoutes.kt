package com.park.routes

import com.park.dto.UserDTO
import com.park.repositories.AccountRepository
import com.park.repositories.IAccountRepository
import com.park.repositories.IUserRepository
import com.park.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(
    userRepository: IUserRepository = UserRepository(),
    accountRepository: IAccountRepository = AccountRepository()
) {
    route("/api/user") {
        /**
         * GET /api/user/profile
         * Lấy thông tin profile của user đang đăng nhập
         * Yêu cầu: JWT token trong header Authorization
         */
        authenticate("auth-jwt") {
            get("/profile") {
                try {
                    // Lấy thông tin từ JWT token
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                    val accountId = principal?.payload?.getClaim("accountId")?.asString()

                    if (userId == null || accountId == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf(
                                "success" to false,
                                "message" to "Invalid token"
                            )
                        )
                        return@get
                    }

                    // Lấy thông tin user từ repository
                    val user = userRepository.findById(userId)
                    if (user == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            mapOf(
                                "success" to false,
                                "message" to "User not found"
                            )
                        )
                        return@get
                    }

                    // Lấy thông tin account để lấy phone number và role
                    val account = accountRepository.findById(accountId)
                    if (account == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            mapOf(
                                "success" to false,
                                "message" to "Account not found"
                            )
                        )
                        return@get
                    }

                    // Tạo UserDTO
                    val userDTO = UserDTO.fromEntity(user, account.phoneNumber, account.role)

                    // Trả về thông tin user
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "success" to true,
                            "message" to "Profile retrieved successfully",
                            "data" to userDTO
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf(
                            "success" to false,
                            "message" to "Internal server error: ${e.message}"
                        )
                    )
                }
            }
        }

        /**
         * GET /api/user/test-auth
         * Endpoint đơn giản để test JWT authentication
         */
        authenticate("auth-jwt") {
            get("/test-auth") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                val accountId = principal?.payload?.getClaim("accountId")?.asString()
                val role = principal?.payload?.getClaim("role")?.asString()

                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "success" to true,
                        "message" to "Authentication successful",
                        "data" to mapOf(
                            "userId" to userId,
                            "accountId" to accountId,
                            "role" to role
                        )
                    )
                )
            }
        }
    }
}
