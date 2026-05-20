package com.park.smartcard.data.repository

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import com.park.smartcard.data.network.ApiClient
import com.park.smartcard.model.SyncGamePlayRequest
import com.park.smartcard.model.UseGameEnvelope

/**
 * Đồng bộ lượt chơi pending từ thiết bị (khi mất mạng) về server.
 * Endpoint trả `UseGameEnvelope` (legacy DTO không phải `ApiResponse`) nên không dùng apiCall helper được.
 */
class GamePlaySyncRepository {

    suspend fun syncPendingGamePlay(play: PendingGamePlay): Result<Unit> {
        return try {
            val response = ApiClient.http.post("/api/games/${play.gameId}/sync-play") {
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
            else Result.failure(Exception(body.message ?: "Lỗi đồng bộ lượt chơi"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
