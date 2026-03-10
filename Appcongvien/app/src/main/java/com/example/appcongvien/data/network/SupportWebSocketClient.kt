package com.example.appcongvien.data.network

import com.example.appcongvien.data.model.SupportMessageDTO
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

data class WsSupportMessage(
    val messageId: String,
    val userId: String,
    val content: String,
    val senderType: String,
    val userName: String? = null,
    val createdAt: String
)

class SupportWebSocketClient(private val baseUrl: String) {

    private val okHttpClient = OkHttpClient()
    private val gson = Gson()
    private var webSocket: WebSocket? = null

    // SharedFlow để emit tin nhắn mới tới các collector
    private val _newMessage = MutableSharedFlow<SupportMessageDTO>(extraBufferCapacity = 64)
    val newMessage: SharedFlow<SupportMessageDTO> = _newMessage

    fun connect(token: String) {
        val wsUrl = baseUrl
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/') + "/ws/support"

        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("Authorization", "Bearer $token")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val ws = gson.fromJson(text, WsSupportMessage::class.java)
                    val dto = SupportMessageDTO(
                        messageId = ws.messageId,
                        userId = ws.userId,
                        content = ws.content,
                        senderType = ws.senderType,
                        createdAt = ws.createdAt
                    )
                    _newMessage.tryEmit(dto)
                } catch (_: Exception) {}
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                // Có thể log lỗi ở đây nếu cần
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Screen closed")
        webSocket = null
    }
}
