package com.park.plugins

import com.park.routes.authRoutes
import com.park.routes.userRoutes
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
    }
}
