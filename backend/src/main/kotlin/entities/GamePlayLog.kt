package com.park.entities

import java.math.BigDecimal
import java.time.Instant

data class GamePlayLog(
    val logId: String,
    val clientTransactionId: String?,
    val userId: String,
    val gameId: String,
    val cardId: String?,
    val method: String,                // CARD | BALANCE
    val amountCharged: BigDecimal,
    val cardBalanceAfter: BigDecimal?,
    val playedAt: Instant
)
