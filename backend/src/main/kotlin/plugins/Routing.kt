package com.park.plugins

import com.park.routes.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Welcome to Park Adventure API!")
        }

        // Authentication routes (đăng ký, đăng nhập)
        authRoutes()

        // User routes (protected với JWT)
        userRoutes()

        // Game routes (public + admin)
        gameRoutes()

        // Card routes (protected với JWT)
        cardRoutes()

        // Voucher routes (public + user + admin)
        voucherRoutes()

        // Order routes (protected với JWT)
        orderRoutes()

        // Wallet routes (protected với JWT)
        walletRoutes()

        // Game review routes (public + user)
        gameReviewRoutes()

        // Notification routes (protected với JWT)
        notificationRoutes()

        // Support chat routes (protected với JWT)
        supportRoutes()

        // Admin routes (public auth + protected management)
        adminRoutes()
    }
}
