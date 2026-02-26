package com.example.appcongvien.data.network

import android.content.Context
import com.example.appcongvien.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Khi chạy trên emulator dùng 10.0.2.2 để trỏ tới localhost của máy tính
    // Khi chạy trên thiết bị thật, thay bằng IP của máy trong cùng mạng LAN
    private const val BASE_URL = "http://192.168.2.7:8080/"

    @Volatile
    private var instance: ApiService? = null

    fun getApiService(context: Context): ApiService {
        return instance ?: synchronized(this) {
            instance ?: buildApiService(context).also { instance = it }
        }
    }

    private fun buildApiService(context: Context): ApiService {
        val tokenManager = TokenManager.getInstance(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenManager.getToken() })
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
