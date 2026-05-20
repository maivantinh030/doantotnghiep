package com.park.smartcard.data.network

import com.park.smartcard.data.model.ApiResponse
import io.ktor.client.call.*
import io.ktor.client.statement.*

/**
 * Bọc pattern try / parse / check / fail-on-null phổ biến.
 * Dùng cho mọi endpoint trả `ApiResponse<T>` với T non-null khi success.
 */
suspend inline fun <reified T> apiCall(
    errorMessage: String,
    crossinline block: suspend () -> HttpResponse
): Result<T> {
    return try {
        val body = block().body<ApiResponse<T>>()
        if (body.success && body.data != null) Result.success(body.data)
        else Result.failure(Exception(body.message ?: errorMessage))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Variant cho endpoint không cần payload trả về (chỉ check `success`).
 * Dùng cho POST/DELETE/PUT mà chỉ quan tâm thành công hay thất bại.
 */
suspend inline fun apiCallUnit(
    errorMessage: String,
    crossinline block: suspend () -> HttpResponse
): Result<Unit> {
    return try {
        val body = block().body<ApiResponse<Unit>>()
        if (body.success) Result.success(Unit)
        else Result.failure(Exception(body.message ?: errorMessage))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Variant cho endpoint trả `ApiResponse<List<T>>` — cho phép list rỗng (data = null hoặc emptyList).
 */
suspend inline fun <reified T> apiCallList(
    errorMessage: String,
    crossinline block: suspend () -> HttpResponse
): Result<List<T>> {
    return try {
        val body = block().body<ApiResponse<List<T>>>()
        if (body.success) Result.success(body.data ?: emptyList())
        else Result.failure(Exception(body.message ?: errorMessage))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
