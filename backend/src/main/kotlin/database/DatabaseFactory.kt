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




private fun dropColumnIfExists(connection: Connection, tableName: String, columnName: String) {
    val sql = """
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = ?
          AND COLUMN_NAME = ?
        LIMIT 1
    """.trimIndent()

    val exists = connection.prepareStatement(sql).use { stmt ->
        stmt.setString(1, tableName)
        stmt.setString(2, columnName)
        stmt.executeQuery().use { rs -> rs.next() }
    }

    if (exists) {
        connection.createStatement().use { ddl ->
            ddl.execute("""ALTER TABLE `$tableName` DROP COLUMN `$columnName`""")
        }
        println("🧹 Dropped legacy column: $tableName.$columnName")
    }
}

private fun dropTableIfExists(connection: Connection, tableName: String) {
    val sql = """
        SELECT 1
        FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = ?
        LIMIT 1
    """.trimIndent()

    val exists = connection.prepareStatement(sql).use { stmt ->
        stmt.setString(1, tableName)
        stmt.executeQuery().use { rs -> rs.next() }
    }

    if (exists) {
        connection.createStatement().use { ddl ->
            ddl.execute("""DROP TABLE `$tableName`""")
        }
        println("🧹 Dropped legacy table: $tableName")
    }
}
