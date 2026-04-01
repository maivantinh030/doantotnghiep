package com.example.appcongvien.data.repository

import android.content.Context
import android.provider.Settings
import com.example.appcongvien.data.local.TokenManager
import com.example.appcongvien.data.model.RegisterPushTokenRequest
import com.example.appcongvien.data.model.UnregisterPushTokenRequest
import com.example.appcongvien.data.network.ApiService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PushTokenRepository(
    context: Context,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun syncCurrentToken() {
        if (!tokenManager.hasToken()) {
            return
        }

        val pendingToken = tokenManager.getPendingPushToken()
        if (!pendingToken.isNullOrBlank() && pendingToken != tokenManager.getSyncedPushToken()) {
            registerToken(pendingToken)
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }

            val token = task.result?.trim().orEmpty()
            if (token.isNotEmpty()) {
                onNewToken(token)
            }
        }
    }

    fun onNewToken(token: String) {
        val normalizedToken = token.trim()
        if (normalizedToken.isBlank()) {
            return
        }

        tokenManager.savePendingPushToken(normalizedToken)

        if (!tokenManager.hasToken()) {
            return
        }

        if (normalizedToken == tokenManager.getSyncedPushToken()) {
            return
        }

        registerToken(normalizedToken)
    }

    fun unregisterCurrentTokenBeforeLogout(authToken: String?) {
        val normalizedAuthToken = authToken?.trim().orEmpty()
        val currentPushToken = tokenManager.getPendingPushToken()
            ?: tokenManager.getSyncedPushToken()

        tokenManager.clearPushTokenState()

        if (normalizedAuthToken.isBlank() || currentPushToken.isNullOrBlank()) {
            return
        }

        scope.launch {
            runCatching {
                apiService.unregisterPushToken(
                    request = UnregisterPushTokenRequest(currentPushToken),
                    authorization = "Bearer $normalizedAuthToken"
                )
            }
        }
    }

    private fun registerToken(token: String) {
        scope.launch {
            runCatching {
                apiService.registerPushToken(
                    RegisterPushTokenRequest(
                        token = token,
                        deviceId = getDeviceId()
                    )
                )
            }.onSuccess { response ->
                if (response.isSuccessful && response.body()?.success == true) {
                    tokenManager.savePendingPushToken(token)
                    tokenManager.saveSyncedPushToken(token)
                }
            }
        }
    }

    private fun getDeviceId(): String? {
        return runCatching {
            Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}
