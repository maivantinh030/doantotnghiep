package com.park.services

import com.park.dto.PushTokenDTO
import com.park.dto.RegisterPushTokenRequest
import com.park.repositories.IUserPushTokenRepository
import com.park.repositories.UserPushTokenRepository

class PushTokenService(
    private val userPushTokenRepository: IUserPushTokenRepository = UserPushTokenRepository()
) {

    fun registerToken(userId: String, request: RegisterPushTokenRequest): PushTokenDTO {
        require(request.token.isNotBlank()) { "Push token must not be blank" }

        val registered = userPushTokenRepository.upsert(
            userId = userId,
            token = request.token,
            platform = request.platform,
            deviceId = request.deviceId
        )

        return PushTokenDTO.fromEntity(registered)
    }

    fun unregisterToken(userId: String, token: String): Boolean {
        require(token.isNotBlank()) { "Push token must not be blank" }

        val existing = userPushTokenRepository.findByToken(token.trim()) ?: return false
        if (existing.userId != userId) return false

        return userPushTokenRepository.deactivateByToken(token)
    }
}
