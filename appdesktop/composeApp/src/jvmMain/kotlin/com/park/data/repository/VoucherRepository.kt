package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class VoucherRepository {

    private fun authHeader() = "Bearer ${ApiClient.getToken()}"

    suspend fun getVouchers(page: Int = 1, size: Int = 20): Result<PaginatedData<VoucherDTO>> {
        return try {
            val response = ApiClient.http.get("/api/vouchers") {
                header(HttpHeaders.Authorization, authHeader())
                parameter("page", page)
                parameter("size", size)
            }
            val body = response.body<ApiResponse<PaginatedData<VoucherDTO>>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi lấy danh sách voucher"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createVoucher(request: CreateVoucherRequest): Result<VoucherDTO> {
        return try {
            val response = ApiClient.http.post("/api/vouchers") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<VoucherDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi tạo voucher"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVoucher(voucherId: String, request: UpdateVoucherRequest): Result<VoucherDTO> {
        return try {
            val response = ApiClient.http.put("/api/vouchers/$voucherId") {
                header(HttpHeaders.Authorization, authHeader())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<VoucherDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi cập nhật voucher"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVoucher(voucherId: String): Result<Unit> {
        return try {
            val response = ApiClient.http.delete("/api/vouchers/$voucherId") {
                header(HttpHeaders.Authorization, authHeader())
            }
            val body = response.body<ApiResponse<Unit>>()
            if (body.success) Result.success(Unit)
            else Result.failure(Exception(body.message ?: "Lỗi xóa voucher"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
