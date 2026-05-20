package com.park.smartcard.network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.park.smartcard.auth.TokenStore
import com.park.smartcard.config.ServerConfig

object ApiClient {
    val http: HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(DefaultRequest) {
            val configBase = ServerConfig.baseUrl.trimEnd('/')
            url(configBase)
            headers.append("Content-Type", "application/json")
            headers.append("Accept", "application/json")
            TokenStore.getToken()?.let { token ->
                headers.append("Authorization", "Bearer $token")
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
        }
    }
}
