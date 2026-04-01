package com.park

import com.park.database.configureDatabase
import com.park.plugins.configureFirebaseAdmin
import com.park.plugins.configureHTTP
import com.park.plugins.configureMonitoring
import com.park.plugins.configureRouting
import com.park.plugins.configureSecurity
import com.park.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(WebSockets) {
        pingPeriod = 20.seconds
        timeout = 30.seconds
    }
    configureSerialization()
    configureDatabase()
    configureFirebaseAdmin()
    configureMonitoring()
    configureSecurity()
    configureHTTP()
    configureRouting()
}
