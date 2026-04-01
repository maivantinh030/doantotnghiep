package org.example.project.network

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.example.project.model.AdminInfo
import org.example.project.model.AdminLoginRequest
import org.example.project.model.AdminLoginResponse
import org.example.project.model.BackendAdminAuthResponse

@Serializable
private data class VerifyTokenResponse(
    val valid: Boolean,
    val admin: AdminInfo? = null
)

class AdminApi {
    fun login(username: String, password: String): AdminLoginResponse = runBlocking {
        try {
            val req = AdminLoginRequest(phoneNumber = username, password = password)
            val response = ApiClient.http.post("/api/admin/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            if (!response.status.isSuccess()) {
                val text = response.bodyAsText()
                return@runBlocking AdminLoginResponse(
                    success = false,
                    message = text.ifEmpty { "HTTP ${response.status.value}" }
                )
            }

            val backend = response.body<BackendAdminAuthResponse>()
            if (!backend.success || backend.data == null) {
                return@runBlocking AdminLoginResponse(
                    success = false,
                    message = backend.message
                )
            }

            val admin = backend.data.admin
            val localAdminInfo = AdminInfo(
                adminId = admin.adminId,
                username = admin.phoneNumber,
                fullName = admin.fullName,
                role = admin.role
            )

            AdminLoginResponse(
                success = true,
                token = backend.data.token,
                adminInfo = localAdminInfo,
                message = backend.message
            )
        } catch (e: Exception) {
            AdminLoginResponse(false, message = e.message ?: "Lỗi kết nối")
        }
    }

    fun verifyToken(): AdminInfo? = runBlocking {
        try {
            val response = ApiClient.http.get("/admin/verify-token")
            if (!response.status.isSuccess()) return@runBlocking null
            
            val dto = response.body<VerifyTokenResponse>()
            if (dto.valid && dto.admin != null) {
                return@runBlocking dto.admin
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
