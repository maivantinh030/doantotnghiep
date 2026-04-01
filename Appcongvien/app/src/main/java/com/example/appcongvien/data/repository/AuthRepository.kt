package com.example.appcongvien.data.repository

import com.example.appcongvien.data.local.TokenManager
import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.network.ApiService

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val pushTokenRepository: PushTokenRepository
) {
    suspend fun register(request: RegisterRequest): Resource<AuthData> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    tokenManager.saveToken(body.data.token)
                    val user = body.data.user
                    tokenManager.saveUserInfo(
                        user.userId, user.fullName, user.phoneNumber, user.currentBalance, user.role
                    )
                    pushTokenRepository.syncCurrentToken()
                    Resource.Success(body.data)
                } else {
                    Resource.Error(body?.message ?: "Đăng ký thất bại")
                }
            } else {
                Resource.Error("Lỗi ${response.code()}: ${response.message()}", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun login(request: LoginRequest): Resource<AuthData> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    tokenManager.saveToken(body.data.token)
                    val user = body.data.user
                    tokenManager.saveUserInfo(
                        user.userId, user.fullName, user.phoneNumber, user.currentBalance, user.role
                    )
                    pushTokenRepository.syncCurrentToken()
                    Resource.Success(body.data)
                } else {
                    Resource.Error(body?.message ?: "Đăng nhập thất bại")
                }
            } else {
                Resource.Error("Số điện thoại hoặc mật khẩu không đúng", response.code())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getUserProfile(): Resource<UserDTO> {
        return try {
            val response = apiService.getUserProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể lấy thông tin người dùng")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun changePassword(request: ChangePasswordRequest): Resource<Unit> {
        return try {
            val response = apiService.changePassword(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Đổi mật khẩu thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    fun logout() {
        pushTokenRepository.unregisterCurrentTokenBeforeLogout(tokenManager.getToken())
        tokenManager.clear()
    }

    fun isLoggedIn() = tokenManager.hasToken()
    fun getCurrentUserId() = tokenManager.getUserId()
    fun getCurrentUserName() = tokenManager.getFullName()
    fun getCurrentBalance() = tokenManager.getBalance()
}
