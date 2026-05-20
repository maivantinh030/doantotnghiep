package com.park.data.network

import com.park.data.model.ApiResponse
import io.ktor.client.call.*
import io.ktor.client.statement.*

/**
 * Helper bọc pattern try / parse / check / fail-on-null phổ biến.
 * Dùng cho mọi endpoint trả `ApiResponse<T>` với T non-null khi success.
 *
 * Ví dụ:
 * ```
 * suspend fun getGames(...): Result<PaginatedData<GameDTO>> =
 *     apiCall("Lỗi lấy danh sách trò chơi") {
 *         ApiClient.http.get("/api/games") { parameter("page", page) }
 *     }
 * ```
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
