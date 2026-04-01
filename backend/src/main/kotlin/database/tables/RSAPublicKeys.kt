package com.park.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object RSAPublicKeys : Table("rsa_public_keys") {
    val cardId = varchar("card_id", 36)        // FK → cards, 1 thẻ = 1 RSA keypair
    val publicKeyPem = text("public_key_pem")
    val status = varchar("status", 20).default("ACTIVE") // ACTIVE | REVOKED
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(cardId)
}
