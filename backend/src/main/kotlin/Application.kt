package com.park

import com.park.database.configureDatabase
import com.park.plugins.configureHTTP
import com.park.plugins.configureMonitoring
import com.park.plugins.configureRouting
import com.park.plugins.configureSecurity
import com.park.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.path
import org.slf4j.event.Level

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureDatabase()
    configureMonitoring()
    configureSecurity()
    configureHTTP()
    configureRouting()
}
