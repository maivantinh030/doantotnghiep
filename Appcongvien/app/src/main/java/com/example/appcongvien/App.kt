package com.example.appcongvien

import android.app.Application
import com.example.appcongvien.data.local.TokenManager
import com.example.appcongvien.data.network.RetrofitClient
import com.example.appcongvien.data.network.SupportWebSocketClient
import com.example.appcongvien.data.repository.*

class App : Application() {
    lateinit var tokenManager: TokenManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var gameRepository: GameRepository
        private set

    lateinit var cardRepository: CardRepository
        private set

    lateinit var cardRequestRepository: CardRequestRepository
        private set

    lateinit var walletRepository: WalletRepository
        private set

    lateinit var notificationRepository: NotificationRepository
        private set

    lateinit var pushTokenRepository: PushTokenRepository
        private set

    lateinit var supportRepository: SupportRepository
        private set

    lateinit var supportWebSocketClient: SupportWebSocketClient
        private set

    lateinit var announcementRepository: AnnouncementRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        tokenManager = TokenManager.getInstance(this)
        val apiService = RetrofitClient.getApiService(this)
        pushTokenRepository = PushTokenRepository(this, apiService, tokenManager)
        authRepository = AuthRepository(apiService, tokenManager, pushTokenRepository)
        gameRepository = GameRepository(apiService)
        cardRepository = CardRepository(apiService)
        cardRequestRepository = CardRequestRepository(apiService)
        walletRepository = WalletRepository(apiService, tokenManager)
        notificationRepository = NotificationRepository(apiService)
        supportRepository = SupportRepository(apiService)
        supportWebSocketClient = SupportWebSocketClient(RetrofitClient.BASE_URL)
        announcementRepository = AnnouncementRepository(apiService)
        PushNotificationHelper.createNotificationChannel(this)
        pushTokenRepository.syncCurrentToken()
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
