package com.park.repositories

import com.park.database.tables.RSAPublicKeys
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

interface IRSAPublicKeyRepository {
    fun upsertActive(cardId: String, publicKeyPem: String)
    fun findActivePublicKeyPem(cardId: String): String?
}

class RSAPublicKeyRepository : IRSAPublicKeyRepository {

    override fun upsertActive(cardId: String, publicKeyPem: String) {
        val now = Instant.now()
        transaction {
            val exists = RSAPublicKeys.selectAll()
                .where { RSAPublicKeys.cardId eq cardId }
                .count() > 0

            if (exists) {
                RSAPublicKeys.update({ RSAPublicKeys.cardId eq cardId }) {
                    it[RSAPublicKeys.publicKeyPem] = publicKeyPem
                    it[status] = "ACTIVE"
                    it[updatedAt] = now
                }
            } else {
                RSAPublicKeys.insert {
                    it[RSAPublicKeys.cardId] = cardId
                    it[RSAPublicKeys.publicKeyPem] = publicKeyPem
                    it[status] = "ACTIVE"
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }
        }
    }

    override fun findActivePublicKeyPem(cardId: String): String? {
        return transaction {
            RSAPublicKeys.selectAll()
                .where { RSAPublicKeys.cardId eq cardId }
                .singleOrNull()
                ?.let { row ->
                    if (row[RSAPublicKeys.status] == "ACTIVE") row[RSAPublicKeys.publicKeyPem] else null
                }
        }
    }
}
