package com.park.smartcard.data.repository

import io.ktor.client.request.*
import io.ktor.http.*
import com.park.smartcard.data.model.CardDTO
import com.park.smartcard.data.model.CardLookupRequest
import com.park.smartcard.data.model.IssueCardRequest
import com.park.smartcard.data.model.RegisterCardRequest
import com.park.smartcard.data.model.ReturnSummary
import com.park.smartcard.data.network.ApiClient
import com.park.smartcard.data.network.apiCall
import com.park.smartcard.data.network.apiCallList

/**
 * Vòng đời thẻ vật lý: đăng ký thẻ trắng, phát hành, trả lại, tra cứu theo UID.
 */
class CardRepository {

    suspend fun getAvailableCards(): Result<List<CardDTO>> =
        apiCallList("Lỗi lấy danh sách thẻ") {
            ApiClient.http.get("/api/cards/available")
        }

    suspend fun registerCard(request: RegisterCardRequest): Result<CardDTO> =
        apiCall("Lỗi đăng ký thẻ") {
            ApiClient.http.post("/api/cards/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun issueCard(request: IssueCardRequest): Result<CardDTO> =
        apiCall("Lỗi phát hành thẻ") {
            ApiClient.http.post("/api/cards/issue") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun returnCard(cardId: String): Result<ReturnSummary> =
        apiCall("Lỗi trả thẻ") {
            ApiClient.http.post("/api/cards/$cardId/return")
        }

    suspend fun lookupCardByCardId(cardId: String): Result<CardDTO> =
        apiCall("Không tìm thấy thẻ") {
            ApiClient.http.post("/api/cards/tap") {
                contentType(ContentType.Application.Json)
                setBody(CardLookupRequest(cardId = cardId))
            }
        }
}
