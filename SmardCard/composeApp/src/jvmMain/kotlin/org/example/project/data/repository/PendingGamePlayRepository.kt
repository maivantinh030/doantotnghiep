package org.example.project.data.repository

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.model.SyncGamePlayRequest
import java.io.File

@Serializable
data class PendingGamePlay(
    val clientTransactionId: String,
    val gameId: String,
    val cardId: String,
    val chargedAmount: String,
    val cardBalanceAfter: String,
    val playedAt: String
) {
    fun toSyncRequest(): SyncGamePlayRequest {
        return SyncGamePlayRequest(
            clientTransactionId = clientTransactionId,
            cardId = cardId,
            chargedAmount = chargedAmount,
            cardBalanceAfter = cardBalanceAfter,
            playedAt = playedAt
        )
    }
}

data class PendingGamePlayFlushResult(
    val syncedCount: Int,
    val remainingCount: Int,
    val failure: Throwable? = null
)

class PendingGamePlayRepository(
    private val storageFile: File = defaultStorageFile()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }
    private val lock = Any()

    fun loadAll(): List<PendingGamePlay> = synchronized(lock) {
        if (!storageFile.exists()) return emptyList()
        runCatching {
            json.decodeFromString<List<PendingGamePlay>>(storageFile.readText())
        }.getOrElse {
            println("Failed to read pending game plays: ${it.message}")
            emptyList()
        }
    }

    fun count(): Int = loadAll().size

    fun enqueue(item: PendingGamePlay) = synchronized(lock) {
        val entries = loadAll().toMutableList()
        entries.removeAll { it.clientTransactionId == item.clientTransactionId }
        entries.add(item)
        save(entries)
    }

    suspend fun flush(syncer: suspend (PendingGamePlay) -> Result<*>): PendingGamePlayFlushResult {
        val snapshot = loadAll().sortedBy { it.playedAt }
        if (snapshot.isEmpty()) {
            return PendingGamePlayFlushResult(syncedCount = 0, remainingCount = 0)
        }

        val remaining = snapshot.toMutableList()
        var syncedCount = 0

        for (item in snapshot) {
            val result = syncer(item)
            if (result.isSuccess) {
                remaining.removeAll { it.clientTransactionId == item.clientTransactionId }
                save(remaining)
                syncedCount++
            } else {
                return PendingGamePlayFlushResult(
                    syncedCount = syncedCount,
                    remainingCount = remaining.size,
                    failure = result.exceptionOrNull()
                )
            }
        }

        return PendingGamePlayFlushResult(syncedCount = syncedCount, remainingCount = remaining.size)
    }

    private fun save(entries: List<PendingGamePlay>) = synchronized(lock) {
        storageFile.parentFile?.mkdirs()
        if (entries.isEmpty()) {
            if (storageFile.exists()) {
                storageFile.delete()
            }
            return
        }
        storageFile.writeText(json.encodeToString(entries))
    }

    companion object {
        private fun defaultStorageFile(): File {
            val home = System.getProperty("user.home").orEmpty().ifBlank { "." }
            return File(home, ".smartcard-park/pending-game-plays.json")
        }
    }
}

fun Throwable?.isLikelyNetworkError(): Boolean {
    val message = this?.message.orEmpty().lowercase()
    return message.contains("connect") ||
        message.contains("timeout") ||
        message.contains("unable to resolve host") ||
        message.contains("network is unreachable") ||
        message.contains("unresolved address") ||
        message.contains("failed to connect") ||
        message.contains("connection refused") ||
        message.contains("broken pipe")
}

fun Throwable?.isAuthError(): Boolean {
    val message = this?.message.orEmpty().lowercase()
    return message.contains("401") ||
        message.contains("403") ||
        message.contains("unauthorized") ||
        message.contains("forbidden") ||
        message.contains("invalid token")
}
