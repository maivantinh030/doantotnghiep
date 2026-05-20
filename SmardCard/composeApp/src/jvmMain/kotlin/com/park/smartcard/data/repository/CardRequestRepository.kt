package com.park.smartcard.data.repository

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import com.park.smartcard.data.model.ApiResponse
import com.park.smartcard.data.model.ApproveCardRequestDTO
import com.park.smartcard.data.model.CardRequestDTO
import com.park.smartcard.data.model.DirectIssueRequest
import com.park.smartcard.data.model.IssueCardFromRequestDTO
import com.park.smartcard.data.network.ApiClient
import com.park.smartcard.data.network.apiCall
import com.park.smartcard.data.network.apiCallList
import com.park.smartcard.data.network.apiCallUnit

/**
 * Yêu cầu cấp thẻ từ app khách (PENDING → APPROVED/REJECTED → COMPLETED/CANCELED)
 * và luồng cấp thẻ trực tiếp tại quầy.
 */
class CardRequestRepository {

    suspend fun getCardRequests(status: String = "PENDING"): Result<List<CardRequestDTO>> =
        apiCallList("Lỗi lấy danh sách yêu cầu") {
            ApiClient.http.get("/api/card-requests") {
                parameter("status", status)
            }
        }

    suspend fun reviewRequest(requestId: String, approved: Boolean, note: String?): Result<CardRequestDTO> =
        apiCall("Lỗi xử lý yêu cầu") {
            ApiClient.http.post("/api/card-requests/$requestId/review") {
                contentType(ContentType.Application.Json)
                setBody(ApproveCardRequestDTO(approved = approved, note = note))
            }
        }

    suspend fun completeRequest(requestId: String): Result<CardRequestDTO> =
        apiCall("Lỗi hoàn thành yêu cầu") {
            ApiClient.http.post("/api/card-requests/$requestId/complete")
        }

    /**
     * Endpoint trả `ApiResponse<CardRequestDTO>` khi 2xx, nhưng có thể trả raw text
     * (vd HTML stack trace) khi gặp lỗi 5xx. Giữ logic check raw body để báo lỗi rõ.
     */
    suspend fun issueCardRequest(requestId: String, cardId: String, publicKey: String): Result<CardRequestDTO> {
        return try {
            val response = ApiClient.http.post("/api/card-requests/$requestId/issue") {
                contentType(ContentType.Application.Json)
                setBody(IssueCardFromRequestDTO(cardId = cardId, publicKey = publicKey))
            }

            if (!response.status.isSuccess()) {
                val rawBody = response.bodyAsText().trim()
                val message = rawBody.ifBlank { "HTTP ${response.status.value} ${response.status.description}" }
                return Result.failure(Exception("Gọi API cấp thẻ thất bại: $message"))
            }

            val body = response.body<ApiResponse<CardRequestDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi cấp thẻ từ yêu cầu"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun directIssue(request: DirectIssueRequest): Result<Unit> =
        apiCallUnit("Lỗi cấp thẻ") {
            ApiClient.http.post("/api/staff/direct-issue") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
}
