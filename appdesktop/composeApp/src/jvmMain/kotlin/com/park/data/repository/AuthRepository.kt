package com.park.data.repository

import com.park.data.model.AuthData
import com.park.data.model.LoginRequest
import com.park.data.network.ApiClient
import com.park.data.network.apiCall
import io.ktor.client.request.*
import io.ktor.http.*

class AuthRepository {

    suspend fun login(phoneNumber: String, password: String): Result<AuthData> {
        return apiCall<AuthData>("Đăng nhập thất bại") {
            ApiClient.http.post("/api/admin/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(phoneNumber = phoneNumber, password = password))
            }
        }.onSuccess { ApiClient.setToken(it.token) }
    }

    fun logout() {
        ApiClient.setToken(null)
    }
}
