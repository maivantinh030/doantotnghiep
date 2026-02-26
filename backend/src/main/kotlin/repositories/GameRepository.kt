package com.park.repositories

import com.park.database.tables.Games
import com.park.entities.Game
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Interface định nghĩa các operations với Game
 */
interface IGameRepository {
    fun create(game: Game): Game
    fun findById(gameId: String): Game?
    fun findAll(limit: Int, offset: Long): List<Game>
    fun findByCategory(category: String, limit: Int, offset: Long): List<Game>
    fun findFeatured(limit: Int): List<Game>
    fun search(query: String, limit: Int, offset: Long): List<Game>
    fun update(gameId: String, updates: Map<String, Any?>): Boolean
    fun delete(gameId: String): Boolean
    fun countAll(): Long
    fun countByCategory(category: String): Long
    fun countBySearch(query: String): Long
    fun findAllCategories(): List<String>
}

/**
 * Implementation của GameRepository
 */
class GameRepository : IGameRepository {

    override fun create(game: Game): Game {
        return transaction {
            Games.insert {
                it[gameId] = game.gameId
                it[name] = game.name
                it[description] = game.description
                it[shortDescription] = game.shortDescription
                it[category] = game.category
                it[pricePerTurn] = game.pricePerTurn
                it[durationMinutes] = game.durationMinutes
                it[location] = game.location
                it[thumbnailUrl] = game.thumbnailUrl
                it[galleryUrls] = game.galleryUrls
                it[ageRequired] = game.ageRequired
                it[heightRequired] = game.heightRequired
                it[maxCapacity] = game.maxCapacity
                it[status] = game.status
                it[riskLevel] = game.riskLevel
                it[isFeatured] = game.isFeatured
                it[averageRating] = game.averageRating
                it[totalReviews] = game.totalReviews
                it[totalPlays] = game.totalPlays
                it[createdAt] = game.createdAt
                it[updatedAt] = game.updatedAt
            }
            game
        }
    }

    override fun findById(gameId: String): Game? {
        return transaction {
            Games.selectAll().where { Games.gameId eq gameId }
                .singleOrNull()
                ?.let { mapRowToGame(it) }
        }
    }

    override fun findAll(limit: Int, offset: Long): List<Game> {
        return transaction {
            Games.selectAll()
                .where { Games.status eq "ACTIVE" }
                .orderBy(Games.createdAt, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { mapRowToGame(it) }
        }
    }

    override fun findByCategory(category: String, limit: Int, offset: Long): List<Game> {
        return transaction {
            Games.selectAll()
                .where { (Games.category eq category) and (Games.status eq "ACTIVE") }
                .orderBy(Games.createdAt, SortOrder.DESC)
                .limit(limit).offset(offset)
                .map { mapRowToGame(it) }
        }
    }

    override fun findFeatured(limit: Int): List<Game> {
        return transaction {
            Games.selectAll()
                .where { (Games.isFeatured eq true) and (Games.status eq "ACTIVE") }
                .orderBy(Games.averageRating, SortOrder.DESC)
                .limit(limit)
                .map { mapRowToGame(it) }
        }
    }

    override fun search(query: String, limit: Int, offset: Long): List<Game> {
        return transaction {
            val searchPattern = "%$query%"
            Games.selectAll()
                .where {
                    ((Games.name like searchPattern) or
                            (Games.shortDescription like searchPattern) or
                            (Games.category like searchPattern)) and
                            (Games.status eq "ACTIVE")
                }
                .orderBy(Games.name, SortOrder.ASC)
                .limit(limit).offset(offset)
                .map { mapRowToGame(it) }
        }
    }

    override fun update(gameId: String, updates: Map<String, Any?>): Boolean {
        return transaction {
            Games.update(where = { Games.gameId eq gameId }) { stmt ->
                updates.forEach { (key, value) ->
                    when (key) {
                        "name" -> stmt[name] = value as String
                        "description" -> stmt[description] = value as? String
                        "shortDescription" -> stmt[shortDescription] = value as? String
                        "category" -> stmt[category] = value as? String
                        "pricePerTurn" -> stmt[pricePerTurn] = value as BigDecimal
                        "durationMinutes" -> stmt[durationMinutes] = value as? Int
                        "location" -> stmt[location] = value as? String
                        "thumbnailUrl" -> stmt[thumbnailUrl] = value as? String
                        "galleryUrls" -> stmt[galleryUrls] = value as? String
                        "ageRequired" -> stmt[ageRequired] = value as? Int
                        "heightRequired" -> stmt[heightRequired] = value as? Int
                        "maxCapacity" -> stmt[maxCapacity] = value as? Int
                        "status" -> stmt[status] = value as String
                        "riskLevel" -> stmt[riskLevel] = value as? Int
                        "isFeatured" -> stmt[isFeatured] = value as Boolean
                        "averageRating" -> stmt[averageRating] = value as BigDecimal
                        "totalReviews" -> stmt[totalReviews] = value as Int
                        "totalPlays" -> stmt[totalPlays] = value as Int
                    }
                }
                stmt[updatedAt] = Instant.now()
            } > 0
        }
    }

    override fun delete(gameId: String): Boolean {
        return transaction {
            Games.deleteWhere { Games.gameId eq gameId } > 0
        }
    }

    override fun countAll(): Long {
        return transaction {
            Games.selectAll().where { Games.status eq "ACTIVE" }.count()
        }
    }

    override fun countByCategory(category: String): Long {
        return transaction {
            Games.selectAll()
                .where { (Games.category eq category) and (Games.status eq "ACTIVE") }
                .count()
        }
    }

    override fun countBySearch(query: String): Long {
        return transaction {
            val searchPattern = "%$query%"
            Games.selectAll()
                .where {
                    ((Games.name like searchPattern) or
                            (Games.shortDescription like searchPattern) or
                            (Games.category like searchPattern)) and
                            (Games.status eq "ACTIVE")
                }
                .count()
        }
    }

    override fun findAllCategories(): List<String> {
        return transaction {
            Games.select(Games.category)
                .where { (Games.category.isNotNull()) and (Games.status eq "ACTIVE") }
                .withDistinct()
                .mapNotNull { it[Games.category] }
        }
    }

    private fun mapRowToGame(row: ResultRow): Game {
        return Game(
            gameId = row[Games.gameId],
            name = row[Games.name],
            description = row[Games.description],
            shortDescription = row[Games.shortDescription],
            category = row[Games.category],
            pricePerTurn = row[Games.pricePerTurn],
            durationMinutes = row[Games.durationMinutes],
            location = row[Games.location],
            thumbnailUrl = row[Games.thumbnailUrl],
            galleryUrls = row[Games.galleryUrls],
            ageRequired = row[Games.ageRequired],
            heightRequired = row[Games.heightRequired],
            maxCapacity = row[Games.maxCapacity],
            status = row[Games.status],
            riskLevel = row[Games.riskLevel],
            isFeatured = row[Games.isFeatured],
            averageRating = row[Games.averageRating],
            totalReviews = row[Games.totalReviews],
            totalPlays = row[Games.totalPlays],
            createdAt = row[Games.createdAt],
            updatedAt = row[Games.updatedAt]
        )
    }
}
