package com.park.database

import com.park.database.tables.Announcements
import com.park.database.tables.GamePlayLogs
import com.park.database.tables.GameReviews

import com.park.database.tables.UserPushTokens
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

fun Application.configureDatabase() {
    // Đọc config từ application.yml
    val config = environment.config
    val url = config.property("datasource.url").getString()
    val user = config.property("datasource.user").getString()
    val password = config.property("datasource.password").getString()

    // Setup HikariCP connection pool
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = url
        username = user
        this.password = password
        driverClassName = "com.mysql.cj.jdbc.Driver"
        maximumPoolSize = 10
        minimumIdle = 2
        connectionTimeout = 30000
        idleTimeout = 600000
        maxLifetime = 1800000
        validate()
    }

    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            UserPushTokens,
            Announcements,
            GamePlayLogs,
            GameReviews
        )
    }


    println("✅ Database connected successfully!")
}

