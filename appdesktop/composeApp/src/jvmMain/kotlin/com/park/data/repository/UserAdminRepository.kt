package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import com.park.data.network.apiCall
import com.park.data.network.apiCallUnit
import io.ktor.client.request.*
import io.ktor.http.*

class UserAdminRepository {

    suspend fun getUsers(page: Int = 1, size: Int = 20, search: String? = null): Result<PaginatedData<UserDTO>> =
        apiCall("Lỗi lấy danh sách người dùng") {
            ApiClient.http.get("/api/admin/users") {
                parameter("page", page)
                parameter("size", size)
                if (!search.isNullOrBlank()) parameter("search", search)
            }
        }

    suspend fun getUserById(userId: String): Result<UserDTO> =
        apiCall("Lỗi lấy thông tin người dùng") {
            ApiClient.http.get("/api/admin/users/$userId")
        }

    suspend fun lockUser(userId: String): Result<Unit> =
        apiCallUnit("Lỗi khóa tài khoản") {
            ApiClient.http.post("/api/admin/users/$userId/lock")
        }

    suspend fun unlockUser(userId: String): Result<Unit> =
        apiCallUnit("Lỗi mở khóa tài khoản") {
            ApiClient.http.post("/api/admin/users/$userId/unlock")
        }

    suspend fun adjustBalance(userId: String, amount: Double, reason: String): Result<Unit> =
        apiCallUnit("Lỗi điều chỉnh số dư") {
            ApiClient.http.post("/api/admin/users/$userId/adjust-balance") {
                contentType(ContentType.Application.Json)
                setBody(AdjustBalanceRequest(amount = amount, reason = reason))
            }
        }
}
