package org.example.project.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.adapter
import org.example.project.model.AdminInfo
import org.example.project.model.AdminLoginRequest
import org.example.project.model.AdminLoginResponse

class AdminApi {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val reqAdapter = moshi.adapter(AdminLoginRequest::class.java)
    private val respAdapter = moshi.adapter(AdminLoginResponse::class.java)
    private val infoAdapter = moshi.adapter(AdminInfo::class.java)

    fun login(username: String, password: String): AdminLoginResponse {
        val bodyJson = reqAdapter.toJson(AdminLoginRequest(username, password))
        ApiClient.post("/admin/login", bodyJson).use { resp ->
            val text = resp.body?.string()
            if (!resp.isSuccessful) {
                // Try parse error body as structure; fallback
                return AdminLoginResponse(
                    success = false,
                    message = text ?: ("HTTP " + resp.code)
                )
            }
            requireNotNull(text) { "Empty response" }
            return respAdapter.fromJson(text) ?: AdminLoginResponse(false, message = "Invalid response")
        }
    }

    fun verifyToken(): AdminInfo? {
        ApiClient.get("/admin/verify-token").use { resp ->
            if (!resp.isSuccessful) return null
            val text = resp.body?.string() ?: return null
            // Response format: { "valid": true, "admin": { ... } }
            val json = moshi.adapter(Map::class.java).fromJson(text) as? Map<*, *> ?: return null
            val admin = json["admin"] ?: return null
            val adminJson = moshi.adapter(Any::class.java).toJson(admin)
            return infoAdapter.fromJson(adminJson)
        }
    }
}
