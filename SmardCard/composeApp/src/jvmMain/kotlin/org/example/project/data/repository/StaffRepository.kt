package org.example.project.data.repository

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import org.example.project.data.model.*
import org.example.project.data.network.ApiClient
import org.example.project.model.SyncGamePlayRequest
import org.example.project.model.UseGameEnvelope

class StaffRepository {

    private fun auth() = "Bearer ${ApiClient.getToken()}"

    // ── Tìm khách theo SĐT ────────────────────────────────────────────────

    suspend fun findCustomerByPhone(phone: String): Result<CustomerDTO> {
        return try {
            val response = ApiClient.http.get("/api/staff/customers/search") {
                header(HttpHeaders.Authorization, auth())
                parameter("phone", phone)
            }
            val body = response.body<ApiResponse<CustomerDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Không tìm thấy khách hàng"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Thẻ available (chưa liên kết) ────────────────────────────────────

    suspend fun getAvailableCards(): Result<List<CardDTO>> {
        return try {
            val response = ApiClient.http.get("/api/cards/available") {
                header(HttpHeaders.Authorization, auth())
            }
            val body = response.body<ApiResponse<List<CardDTO>>>()
            if (body.success) Result.success(body.data ?: emptyList())
            else Result.failure(Exception(body.message ?: "Lỗi lấy danh sách thẻ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Đăng ký thẻ trắng vào hệ thống ──────────────────────────────────

    suspend fun registerCard(request: RegisterCardRequest): Result<CardDTO> {
        return try {
            val response = ApiClient.http.post("/api/cards/register") {
                header(HttpHeaders.Authorization, auth())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<CardDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi đăng ký thẻ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Phát hành thẻ cho khách ───────────────────────────────────────────

    suspend fun issueCard(request: IssueCardRequest): Result<CardDTO> {
        return try {
            val response = ApiClient.http.post("/api/cards/issue") {
                header(HttpHeaders.Authorization, auth())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<CardDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi phát hành thẻ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Trả thẻ + hoàn cọc + hoàn balance ───────────────────────────────

    suspend fun returnCard(cardId: String): Result<ReturnSummary> {
        return try {
            val response = ApiClient.http.post("/api/cards/$cardId/return") {
                header(HttpHeaders.Authorization, auth())
            }
            val body = response.body<ApiResponse<ReturnSummary>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi trả thẻ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Lấy yêu cầu cấp thẻ từ app ──────────────────────────────────────

    suspend fun getCardRequests(status: String = "PENDING"): Result<List<CardRequestDTO>> {
        return try {
            val response = ApiClient.http.get("/api/card-requests") {
                header(HttpHeaders.Authorization, auth())
                parameter("status", status)
            }
            val body = response.body<ApiResponse<List<CardRequestDTO>>>()
            if (body.success) Result.success(body.data ?: emptyList())
            else Result.failure(Exception(body.message ?: "Lỗi lấy danh sách yêu cầu"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Duyệt / từ chối yêu cầu cấp thẻ ────────────────────────────────

    suspend fun reviewRequest(requestId: String, approved: Boolean, note: String?): Result<CardRequestDTO> {
        return try {
            val response = ApiClient.http.post("/api/card-requests/$requestId/review") {
                header(HttpHeaders.Authorization, auth())
                contentType(ContentType.Application.Json)
                setBody(ApproveCardRequestDTO(approved = approved, note = note))
            }
            val body = response.body<ApiResponse<CardRequestDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi xử lý yêu cầu"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Hoàn thành yêu cầu (sau khi phát thẻ) ───────────────────────────

    suspend fun completeRequest(requestId: String): Result<CardRequestDTO> {
        return try {
            val response = ApiClient.http.post("/api/card-requests/$requestId/complete") {
                header(HttpHeaders.Authorization, auth())
            }
            val body = response.body<ApiResponse<CardRequestDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi hoàn thành yêu cầu"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun issueCardRequest(requestId: String, cardId: String, publicKey: String): Result<CardRequestDTO> {
        return try {
            val response = ApiClient.http.post("/api/card-requests/$requestId/issue") {
                header(HttpHeaders.Authorization, auth())
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

    // ── Tra cứu thẻ theo Physical UID (quẹt thẻ NFC) ────────────────────

    suspend fun lookupCardByCardId(cardId: String): Result<CardDTO> {
        return try {
            val response = ApiClient.http.post("/api/cards/tap") {
                header(HttpHeaders.Authorization, auth())
                contentType(ContentType.Application.Json)
                setBody(CardLookupRequest(cardId = cardId))
            }
            val body = response.body<ApiResponse<CardDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Không tìm thấy thẻ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Lấy thông tin khách theo userId ──────────────────────────────────

    suspend fun getCustomerById(userId: String): Result<CustomerDTO> {
        return try {
            val response = ApiClient.http.get("/api/staff/customers/$userId") {
                header(HttpHeaders.Authorization, auth())
            }
            val body = response.body<ApiResponse<CustomerDTO>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Không tìm thấy khách hàng"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Cấp thẻ trực tiếp (ghi thẻ → lưu user + card + RSA key) ─────────

    suspend fun directIssue(request: DirectIssueRequest): Result<Unit> {
        return try {
            val response = ApiClient.http.post("/api/staff/direct-issue") {
                header(HttpHeaders.Authorization, auth())
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.body<ApiResponse<Map<String, String>>>()
            if (body.success) Result.success(Unit)
            else Result.failure(Exception(body.message ?: "Lỗi cấp thẻ"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Nạp tiền cho khách tại quầy ─────────────────────────────────────

    suspend fun syncPendingGamePlay(play: PendingGamePlay): Result<Unit> {
        return try {
            val response = ApiClient.http.post("/api/games/${play.gameId}/sync-play") {
                header(HttpHeaders.Authorization, auth())
                contentType(ContentType.Application.Json)
                setBody(
                    SyncGamePlayRequest(
                        clientTransactionId = play.clientTransactionId,
                        cardId = play.cardId,
                        chargedAmount = play.chargedAmount,
                        cardBalanceAfter = play.cardBalanceAfter,
                        playedAt = play.playedAt
                    )
                )
            }
            val body = response.body<UseGameEnvelope>()
            if (body.success) Result.success(Unit)
            else Result.failure(Exception(body.message ?: "Loi dong bo luot choi"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun topUpForCustomer(userId: String, amount: String): Result<TopUpResult> {
        return try {
            val response = ApiClient.http.post("/api/staff/customers/$userId/topup") {
                header(HttpHeaders.Authorization, auth())
                contentType(ContentType.Application.Json)
                setBody(TopUpRequest(amount = amount, method = "CASH"))
            }
            val body = response.body<ApiResponse<TopUpResult>>()
            if (body.success && body.data != null) Result.success(body.data)
            else Result.failure(Exception(body.message ?: "Lỗi nạp tiền"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
