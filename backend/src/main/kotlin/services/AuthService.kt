package com.park.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.park.dto.CreateAccountDTO
import com.park.dto.CreateUserDTO
import com.park.dto.UserDTO
import com.park.models.*
import com.park.repositories.AccountRepository
import com.park.repositories.IAccountRepository
import com.park.repositories.IUserRepository
import com.park.repositories.UserRepository
import org.mindrot.jbcrypt.BCrypt
import java.util.*

/**
 * Service xử lý Authentication với Repository pattern
 */
class AuthService(
    private val accountRepository: IAccountRepository = AccountRepository(),
    private val userRepository: IUserRepository = UserRepository()
) {

    companion object {
        private const val JWT_SECRET = "park-adventure-secret-key-2024" // TODO: Move to config
        private const val JWT_ISSUER = "park-adventure"
        private const val JWT_AUDIENCE = "park-adventure-users"
        private const val JWT_VALIDITY_MS = 86400000L * 30 // 30 days
    }

    /**
     * Đăng ký tài khoản mới
     */
    fun register(request: RegisterRequest): AuthResponse {
        return try {
            // Validate input
            val validationErrors = validateRegistration(request)
            if (validationErrors.isNotEmpty()) {
                return AuthResponse(
                    success = false,
                    message = "Validation failed: ${validationErrors.values.first()}",
                    data = null
                )
            }

            // Kiểm tra số điện thoại đã tồn tại chưa
            if (accountRepository.existsByPhoneNumber(request.phoneNumber)) {
                return AuthResponse(
                    success = false,
                    message = "Số điện thoại đã được sử dụng"
                )
            }

            // Kiểm tra email đã tồn tại chưa (nếu có)
            if (!request.email.isNullOrBlank() && userRepository.existsByEmail(request.email)) {
                return AuthResponse(
                    success = false,
                    message = "Email đã được sử dụng"
                )
            }

            // Tạo account
            val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
            val account = accountRepository.create(
                CreateAccountDTO(
                    phoneNumber = request.phoneNumber,
                    passwordHash = hashedPassword,
                    role = "USER"
                )
            )

            // Tạo user profile
            val user = userRepository.create(
                CreateUserDTO(
                    accountId = account.accountId,
                    fullName = request.fullName,
                    email = request.email,
                    dateOfBirth = request.dateOfBirth,
                    gender = request.gender
                )
            )

            // Tạo JWT token
            val token = generateToken(account.accountId, user.userId, account.role)

            // Tạo UserDTO
            val userDTO = UserDTO.fromEntity(user, account.phoneNumber, account.role)

            val userInfo = UserInfo(
                userId = userDTO.userId,
                accountId = userDTO.accountId ?: "",
                phoneNumber = userDTO.phoneNumber,
                fullName = userDTO.fullName,
                email = userDTO.email,
                role = userDTO.role,
                currentBalance = userDTO.currentBalance,
                avatarUrl = userDTO.avatarUrl
            )

            AuthResponse(
                success = true,
                message = "Đăng ký thành công",
                data = AuthData(token = token, user = userInfo)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResponse(
                success = false,
                message = "Lỗi hệ thống: ${e.message}"
            )
        }
    }

    /**
     * Đăng nhập
     */
    fun login(request: LoginRequest): AuthResponse {
        return try {
            // Tìm account theo số điện thoại
            val account = accountRepository.findByPhoneNumber(request.phoneNumber)
                ?: return AuthResponse(
                    success = false,
                    message = "Số điện thoại hoặc mật khẩu không đúng"
                )

            // Kiểm tra status
            if (account.status == "BANNED") {
                return AuthResponse(
                    success = false,
                    message = "Tài khoản đã bị khóa"
                )
            }

            // Verify password
            if (!BCrypt.checkpw(request.password, account.passwordHash)) {
                return AuthResponse(
                    success = false,
                    message = "Số điện thoại hoặc mật khẩu không đúng"
                )
            }

            // Update last login
            accountRepository.updateLastLogin(account.accountId)

            // Lấy thông tin user
            val user = userRepository.findByAccountId(account.accountId)
                ?: return AuthResponse(
                    success = false,
                    message = "Không tìm thấy thông tin người dùng"
                )

            // Tạo JWT token
            val token = generateToken(account.accountId, user.userId, account.role)

            // Tạo UserDTO
            val userDTO = UserDTO.fromEntity(user, account.phoneNumber, account.role)

            val userInfo = UserInfo(
                userId = userDTO.userId,
                accountId = userDTO.accountId ?: "",
                phoneNumber = userDTO.phoneNumber,
                fullName = userDTO.fullName,
                email = userDTO.email,
                role = userDTO.role,
                currentBalance = userDTO.currentBalance,
                avatarUrl = userDTO.avatarUrl
            )

            AuthResponse(
                success = true,
                message = "Đăng nhập thành công",
                data = AuthData(token = token, user = userInfo)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResponse(
                success = false,
                message = "Lỗi hệ thống: ${e.message}"
            )
        }
    }

    /**
     * Tạo JWT token
     */
    private fun generateToken(accountId: String, userId: String, role: String): String {
        return JWT.create()
            .withAudience(JWT_AUDIENCE)
            .withIssuer(JWT_ISSUER)
            .withClaim("accountId", accountId)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + JWT_VALIDITY_MS))
            .sign(Algorithm.HMAC256(JWT_SECRET))
    }

    /**
     * Validate registration input
     */
    private fun validateRegistration(request: RegisterRequest): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        // Validate phone number
        if (request.phoneNumber.isBlank()) {
            errors["phoneNumber"] = "Số điện thoại không được để trống"
        } else if (!request.phoneNumber.matches(Regex("^0\\d{9}$"))) {
            errors["phoneNumber"] = "Số điện thoại không hợp lệ (phải có 10 số và bắt đầu bằng 0)"
        }

        // Validate password
        if (request.password.isBlank()) {
            errors["password"] = "Mật khẩu không được để trống"
        } else if (request.password.length < 6) {
            errors["password"] = "Mật khẩu phải có ít nhất 6 ký tự"
        }

        // Validate email (if provided)
        if (!request.email.isNullOrBlank()) {
            if (!request.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
                errors["email"] = "Email không hợp lệ"
            }
        }

        // Validate gender (if provided)
        if (!request.gender.isNullOrBlank()) {
            if (request.gender !in listOf("MALE", "FEMALE", "OTHER")) {
                errors["gender"] = "Giới tính phải là MALE, FEMALE hoặc OTHER"
            }
        }

        return errors
    }
}
