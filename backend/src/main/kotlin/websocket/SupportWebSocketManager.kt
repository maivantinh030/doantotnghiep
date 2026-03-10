package com.park.websocket

import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap

object SupportWebSocketManager {

    // userId -> session (users)
    private val userSessions = ConcurrentHashMap<String, WebSocketSession>()

    // adminId -> session (admins)
    private val adminSessions = ConcurrentHashMap<String, WebSocketSession>()

    fun addUserSession(userId: String, session: WebSocketSession) {
        userSessions[userId] = session
    }

    fun removeUserSession(userId: String) {
        userSessions.remove(userId)
    }

    fun addAdminSession(adminId: String, session: WebSocketSession) {
        adminSessions[adminId] = session
    }

    fun removeAdminSession(adminId: String) {
        adminSessions.remove(adminId)
    }

    /** Gửi tin nhắn đến đúng user đang kết nối */
    suspend fun sendToUser(userId: String, json: String) {
        try {
            userSessions[userId]?.send(Frame.Text(json))
        } catch (e: Exception) {
            userSessions.remove(userId)
        }
    }

    /** Broadcast tin nhắn đến tất cả admin đang kết nối */
    suspend fun broadcastToAdmins(json: String) {
        val dead = mutableListOf<String>()
        adminSessions.forEach { (id, session) ->
            try {
                session.send(Frame.Text(json))
            } catch (e: Exception) {
                dead.add(id)
            }
        }
        dead.forEach { adminSessions.remove(it) }
    }
}
