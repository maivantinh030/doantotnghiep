package org.example.project.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.example.project.auth.TokenStore
import org.example.project.config.ServerConfig
import java.util.concurrent.TimeUnit

object ApiClient {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Accept", "application/json")

        TokenStore.getToken()?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }

        chain.proceed(builder.build())
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun post(path: String, jsonBody: String): Response {
        val url = ServerConfig.baseUrl.trimEnd('/') + path
        val req = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()
        return client.newCall(req).execute()
    }

    fun get(path: String): Response {
        val url = ServerConfig.baseUrl.trimEnd('/') + path
        val req = Request.Builder()
            .url(url)
            .get()
            .build()
        return client.newCall(req).execute()
    }

    fun delete(path: String): Response {
        val url = ServerConfig.baseUrl.trimEnd('/') + path
        val req = Request.Builder()
            .url(url)
            .delete()
            .build()
        return client.newCall(req).execute()
    }
}
