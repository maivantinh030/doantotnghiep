package com.example.appcongvien.data.local

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "park_adventure_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_PHONE = "phone_number"
        private const val KEY_BALANCE = "current_balance"
        private const val KEY_ROLE = "role"

        @Volatile
        private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun hasToken(): Boolean = getToken() != null

    fun saveUserInfo(userId: String, fullName: String, phone: String, balance: String, role: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_PHONE, phone)
            .putString(KEY_BALANCE, balance)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)
    fun getPhone(): String? = prefs.getString(KEY_PHONE, null)
    fun getBalance(): String? = prefs.getString(KEY_BALANCE, "0")
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun updateBalance(balance: String) {
        prefs.edit().putString(KEY_BALANCE, balance).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
