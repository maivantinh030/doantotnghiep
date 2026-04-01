package com.park.routes

import com.park.database.tables.Cards
import com.park.database.tables.Users
import com.park.dto.*
import com.park.entities.Card
import com.park.models.ErrorResponse
import com.park.repositories.AccountRepository
import com.park.repositories.CardRepository
import com.park.repositories.UserRepository
import com.park.services.RSAService
import com.park.services.WalletService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Route.staffRoutes() {
    val accountRepository = AccountRepository()
    val userRepository = UserRepository()
    val cardRepository = CardRepository()
    val rsaService = RSAService()
    val walletService = WalletService()

    route("/api/staff") {
        authenticate("auth-jwt") {

            /**
             * POST /api/staff/direct-issue
             * Staff: cấp thẻ trực tiếp — tạo user + card + RSA key trong một lần
             * Body: DirectIssueRequest
             */
            post("/direct-issue") {
                try {
                    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Staff/Admin được thực hiện")
                        )
                    }

                    val req = call.receive<DirectIssueRequest>()

                    if (req.customerID.isBlank() || req.cardID.isBlank() ||
                        req.fullName.isBlank() || req.phoneNumber.isBlank() || req.publicKey.isBlank()
                    ) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Thiếu thông tin bắt buộc")
                        )
                    }

                    // Kiểm tra trùng
                    if (userRepository.findById(req.customerID) != null) {
                        return@post call.respond(
                            HttpStatusCode.Conflict,
                            ErrorResponse(message = "Mã khách hàng đã tồn tại: ${req.customerID}")
                        )
                    }
                    if (cardRepository.findById(req.cardID) != null) {
                        return@post call.respond(
                            HttpStatusCode.Conflict,
                            ErrorResponse(message = "Mã thẻ đã tồn tại: ${req.cardID}")
                        )
                    }
                    if (accountRepository.existsByPhoneNumber(req.phoneNumber)) {
                        return@post call.respond(
                            HttpStatusCode.Conflict,
                            ErrorResponse(message = "Số điện thoại đã được đăng ký: ${req.phoneNumber}")
                        )
                    }

                    val now = Instant.now()

                    // 1. Tạo user với userId = customerID (không cần account — chỉ dùng thẻ vật lý)
                    transaction {
                        Users.insert {
                            it[Users.userId] = req.customerID
                            it[Users.accountId] = null
                            it[Users.fullName] = req.fullName
                            it[Users.email] = null
                            it[Users.dateOfBirth] = req.dateOfBirth?.let { d ->
                                LocalDate.parse(d, DateTimeFormatter.ISO_DATE)
                            }
                            it[Users.gender] = null
                            it[Users.currentBalance] = BigDecimal.ZERO
                            it[Users.createdAt] = now
                            it[Users.updatedAt] = now
                        }
                    }

                    // 2. Tạo card (ACTIVE, liên kết với user)
                    cardRepository.create(
                        Card(
                            cardId = req.cardID,
                            userId = req.customerID,
                            cardName = null,
                            status = "ACTIVE",
                            depositAmount = BigDecimal.ZERO,
                            depositStatus = "NONE",
                            issuedAt = now,
                            blockedAt = null,
                            blockedReason = null,
                            lastUsedAt = null,
                            createdAt = now,
                            updatedAt = now
                        )
                    )

                    // 3. Đăng ký RSA public key
                    rsaService.registerPublicKey(req.cardID, req.publicKey).getOrElse { e ->
                        return@post call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse(message = "Lưu public key thất bại: ${e.message}")
                        )
                    }

                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "success" to true,
                            "message" to "Cấp thẻ thành công",
                            "data" to mapOf(
                                "customerID" to req.customerID,
                                "cardID" to req.cardID
                            )
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(message = e.message ?: "Lỗi hệ thống")
                    )
                }
            }

            /**
             * GET /api/staff/customers/search?phone=...
             * Staff: tìm khách hàng theo số điện thoại
             */
            get("/customers/search") {
                try {
                    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@get call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Staff/Admin được thực hiện")
                        )
                    }

                    val phone = call.request.queryParameters["phone"]?.trim()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Thiếu tham số phone")
                        )

                    if (phone.length < 9) {
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Số điện thoại không hợp lệ")
                        )
                    }

                    val account = accountRepository.findByPhoneNumber(phone)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(message = "Không tìm thấy tài khoản với SĐT này")
                        )

                    if (account.role != "USER") {
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Tài khoản này không phải tài khoản khách hàng")
                        )
                    }

                    val user = userRepository.findByAccountId(account.accountId)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(message = "Tài khoản chưa có hồ sơ người dùng")
                        )

                    val userDTO = UserDTO.fromEntity(user, account.phoneNumber, account.role)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to userDTO))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(message = e.message ?: "Lỗi hệ thống")
                    )
                }
            }

            /**
             * GET /api/staff/customers/{userId}
             * Staff: xem chi tiết khách hàng theo userId
             */
            get("/customers/{userId}") {
                try {
                    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@get call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Staff/Admin được thực hiện")
                        )
                    }

                    val userId = call.parameters["userId"]
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Thiếu userId")
                        )

                    val user = userRepository.findById(userId)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(message = "Không tìm thấy người dùng")
                        )

                    val account = user.accountId?.let { accountRepository.findById(it) }
                    val userDTO = UserDTO.fromEntity(user, account?.phoneNumber ?: "", account?.role ?: "USER")
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to userDTO))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(message = e.message ?: "Lỗi hệ thống")
                    )
                }
            }

            /**
             * POST /api/staff/customers/{userId}/topup
             * Staff: nạp tiền cho khách tại quầy (CASH)
             * Body: { amount: "50000", method: "CASH" }
             */
            post("/customers/{userId}/topup") {
                try {
                    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
                    if (role !in listOf("STAFF", "ADMIN")) {
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse(message = "Chỉ Staff/Admin được thực hiện")
                        )
                    }

                    val userId = call.parameters["userId"]
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = "Thiếu userId")
                        )

                    val request = call.receive<TopUpRequest>()
                    val result = walletService.topUp(userId, request)

                    result.fold(
                        onSuccess = { payment ->
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf(
                                    "success" to true,
                                    "message" to "Nạp tiền thành công",
                                    "data" to payment
                                )
                            )
                        },
                        onFailure = { e ->
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse(message = e.message ?: "Lỗi nạp tiền")
                            )
                        }
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = e.message ?: "Lỗi hệ thống")
                    )
                }
            }
        }
    }
}
