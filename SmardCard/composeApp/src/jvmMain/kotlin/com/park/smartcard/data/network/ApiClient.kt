package com.park.smartcard.data.network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.park.smartcard.config.ServerConfig

object ApiClient {

    @Volatile
    private var authToken: String? = null

    fun setToken(token: String?) {
        authToken = token
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
            url(ServerConfig.baseUrl.trimEnd('/'))
            headers.append("Content-Type", "application/json")
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
        }
    }
}
