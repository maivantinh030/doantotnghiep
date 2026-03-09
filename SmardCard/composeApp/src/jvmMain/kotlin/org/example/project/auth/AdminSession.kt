package org.example.project.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.project.model.AdminInfo
import org.example.project.network.AdminApi

class AdminSession(private val api: AdminApi = AdminApi()) {
    var currentAdmin: AdminInfo? = null
        private set

    suspend fun login(username: String, password: String): Result<AdminInfo> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resp = api.login(username, password)
            if (resp.success && !resp.token.isNullOrBlank() && resp.adminInfo != null) {
                TokenStore.setToken(resp.token)
                currentAdmin = resp.adminInfo
                Result.success(resp.adminInfo)
            } else {
                Result.failure(IllegalArgumentException(resp.message ?: "Đăng nhập thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        TokenStore.clear()
        currentAdmin = null
    }
}
