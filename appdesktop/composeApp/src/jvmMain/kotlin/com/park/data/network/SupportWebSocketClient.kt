package com.park.data.network

import com.park.data.model.SupportMessageDTO
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WsSupportMessage(
    val messageId: String,
    val userId: String,
    val content: String,
    val senderType: String,
    val userName: String? = null,
    val createdAt: String
)

class SupportWebSocketClient(private val token: String) {

    private val client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    private val _newMessage = MutableSharedFlow<SupportMessageDTO>(extraBufferCapacity = 64)
    val newMessage: SharedFlow<SupportMessageDTO> = _newMessage

    private var job: Job? = null

    fun connect(scope: CoroutineScope) {
        job = scope.launch {
            val wsUrl = ApiClient.BASE_URL
                .replace("http://", "ws://")
                .replace("https://", "wss://")
                .trimEnd('/') + "/ws/admin/support"

            while (true) {
                try {
                    client.webSocket(
                        urlString = wsUrl,
                        request = { headers.append("Authorization", "Bearer $token") }
                    ) {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                try {
                                    val ws = Json.decodeFromString<WsSupportMessage>(frame.readText())
                                    _newMessage.emit(SupportMessageDTO(
                                        messageId = ws.messageId,
                                        userId = ws.userId,
                                        userName = ws.userName,
                                        content = ws.content,
                                        isFromAdmin = ws.senderType == "ADMIN",
                                        createdAt = ws.createdAt
                                    ))
                                } catch (_: Exception) {}
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Kết nối bị ngắt, thử lại sau 5 giây
                    delay(5000)
                }
            }
        }
    }

    fun disconnect() {
        job?.cancel()
        client.close()
    }
}
