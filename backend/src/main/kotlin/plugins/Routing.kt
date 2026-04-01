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

        authRoutes()
        userRoutes()
        gameRoutes()
        cardRoutes()
        cardRequestRoutes()
        walletRoutes()
        gameReviewRoutes()
        notificationRoutes()
        supportRoutes()
        adminRoutes()
        announcementRoutes()
        supportWebSocketRoutes()
        uploadRoutes()
        rsaRoutes()
        staffRoutes()
    }
}
