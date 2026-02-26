package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Admins : Table("admins") {
    val adminId = varchar("admin_id", 36)
    val accountId = varchar("account_id", 36).uniqueIndex()
    val fullName = varchar("full_name", 100)
    val employeeCode = varchar("employee_code", 20).nullable().uniqueIndex()
    val lastActionAt = timestamp("last_action_at").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(adminId)
}
