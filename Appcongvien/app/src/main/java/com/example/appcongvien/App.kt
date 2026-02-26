package com.example.appcongvien

import android.app.Application
import com.example.appcongvien.data.local.TokenManager
import com.example.appcongvien.data.network.RetrofitClient
import com.example.appcongvien.data.repository.*
import com.example.appcongvien.viewmodel.CartViewModel

class App : Application() {
    lateinit var tokenManager: TokenManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var gameRepository: GameRepository
        private set

    lateinit var cardRepository: CardRepository
        private set

    lateinit var walletRepository: WalletRepository
        private set

    lateinit var voucherRepository: VoucherRepository
        private set

    lateinit var orderRepository: OrderRepository
        private set

    lateinit var notificationRepository: NotificationRepository
        private set

    lateinit var supportRepository: SupportRepository
        private set
    
    // Cart ViewModel - shared across app
    lateinit var cartViewModel: CartViewModel
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        tokenManager = TokenManager.getInstance(this)
        val apiService = RetrofitClient.getApiService(this)
        authRepository = AuthRepository(apiService, tokenManager)
        gameRepository = GameRepository(apiService)
        cardRepository = CardRepository(apiService)
        walletRepository = WalletRepository(apiService, tokenManager)
        voucherRepository = VoucherRepository(apiService)
        orderRepository = OrderRepository(apiService)
        notificationRepository = NotificationRepository(apiService)
        supportRepository = SupportRepository(apiService)
        cartViewModel = CartViewModel()
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
