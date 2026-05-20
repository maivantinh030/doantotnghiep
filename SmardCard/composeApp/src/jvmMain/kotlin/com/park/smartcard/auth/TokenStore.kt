package com.park.smartcard.auth

object TokenStore {
    @Volatile
    private var token: String? = null

    fun setToken(value: String?) {
        token = value
    }

    fun getToken(): String? = token

    fun clear() {
        token = null
    }
}
