package org.example.project.data.repository

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.example.project.data.model.AdminLoginRequest
import org.example.project.data.model.ApiResponse
import org.example.project.data.model.AuthData
import org.example.project.data.network.ApiClient

class AuthRepository {

    suspend fun login(phoneNumber: String, password: String): Result<AuthData> {
        return try {
            val response = ApiClient.http.post("/api/admin/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(AdminLoginRequest(phoneNumber = phoneNumber, password = password))
            }
            val body = response.body<ApiResponse<AuthData>>()
            if (body.success && body.data != null) {
                ApiClient.setToken(body.data.token)
                Result.success(body.data)
            } else {
                Result.failure(Exception(body.message ?: "Đăng nhập thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        ApiClient.setToken(null)
    }
}
