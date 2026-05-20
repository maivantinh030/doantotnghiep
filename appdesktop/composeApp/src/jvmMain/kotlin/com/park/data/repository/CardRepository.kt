package com.park.data.repository

import com.park.data.model.*
import com.park.data.network.ApiClient
import com.park.data.network.apiCall
import io.ktor.client.request.*
import io.ktor.http.*

class CardRepository {

    // Danh sách thẻ AVAILABLE (chưa liên kết)
    suspend fun getAvailableCards(): Result<List<CardDTO>> =
        apiCall("Lỗi lấy danh sách thẻ") {
            ApiClient.http.get("/api/cards/available")
        }

    // Đăng ký thẻ trắng vào hệ thống
    suspend fun registerCard(request: RegisterCardRequest): Result<CardDTO> =
        apiCall("Lỗi đăng ký thẻ") {
            ApiClient.http.post("/api/cards/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    // Phát hành thẻ cho khách (liên kết + thu cọc)
    suspend fun issueCard(request: IssueCardRequest): Result<CardDTO> =
        apiCall("Lỗi phát hành thẻ") {
            ApiClient.http.post("/api/cards/issue") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    // Xử lý trả thẻ
    suspend fun returnCard(cardId: String): Result<CardReturnSummaryDTO> =
        apiCall("Lỗi trả thẻ") {
            ApiClient.http.post("/api/cards/$cardId/return")
        }

    // Khóa thẻ mất
    suspend fun blockCard(cardId: String, reason: String?): Result<CardDTO> =
        apiCall("Lỗi khóa thẻ") {
            ApiClient.http.post("/api/cards/$cardId/block") {
                contentType(ContentType.Application.Json)
                setBody(BlockCardRequest(reason = reason))
            }
        }

    // Danh sách yêu cầu cấp thẻ từ app
    suspend fun getCardRequests(status: String = "PENDING"): Result<List<CardRequestDTO>> =
        apiCall("Lỗi lấy danh sách yêu cầu") {
            ApiClient.http.get("/api/card-requests") {
                parameter("status", status)
            }
        }

    // Duyệt / từ chối yêu cầu cấp thẻ
    suspend fun reviewCardRequest(requestId: String, approved: Boolean, note: String?): Result<CardRequestDTO> =
        apiCall("Lỗi duyệt yêu cầu") {
            ApiClient.http.post("/api/card-requests/$requestId/review") {
                contentType(ContentType.Application.Json)
                setBody(ApproveCardRequestDTO(approved = approved, note = note))
            }
        }

    // Đánh dấu hoàn thành (sau khi đã phát thẻ thực tế)
    suspend fun completeCardRequest(requestId: String): Result<CardRequestDTO> =
        apiCall("Lỗi hoàn thành yêu cầu") {
            ApiClient.http.post("/api/card-requests/$requestId/complete")
        }
}
