package com.park.data.network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {

    // Override bằng env var PARK_API_BASE để chạy production (vd: https://api.example.com)
    val BASE_URL: String = System.getenv("PARK_API_BASE")?.takeIf { it.isNotBlank() }
        ?: "http://localhost:8080"

    // Khôi phục token từ phiên trước (nếu có) — admin không phải login lại mỗi lần mở app.
    private var authToken: String? = TokenStore.load()

    fun setToken(token: String?) {
        authToken = token
        if (token.isNullOrBlank()) {
            TokenStore.clear()
        } else {
            TokenStore.save(token)
        }
        // Force Ktor reload token cho request kế tiếp (không thì nó cache token cũ đến khi 401)
        http.authProvider<BearerAuthProvider>()?.clearToken()
    }

    fun getToken(): String? = authToken

    val http: HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            // HEADERS thay vì INFO để tránh log body chứa password / token plaintext
            level = LogLevel.HEADERS
        }
        install(Auth) {
            bearer {
                loadTokens {
                    authToken?.takeIf { it.isNotBlank() }?.let { BearerTokens(it, "") }
                }
                // Gửi Authorization header ngay từ request đầu tiên, không đợi 401 challenge.
                sendWithoutRequest { true }
            }
        }
        install(DefaultRequest) {
            url(BASE_URL)
            headers.append("Content-Type", "application/json")
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
        }
    }
}
