package com.example.testnfc.data.model

data class AdminLoginRequest(
    val phoneNumber: String,
    val password: String
)

data class AdminAuthData(
    val token: String,
    val admin: AdminInfo
)

data class AdminInfo(
    val adminId: String,
    val fullName: String,
    val phoneNumber: String
)

data class AdminAuthResponse(
    val success: Boolean,
    val message: String,
    val data: AdminAuthData?
)

// Games
data class GameItem(
    val gameId: String,
    val name: String,
    val shortDescription: String?,
    val category: String?,
    val thumbnailUrl: String?,
    val status: String,
    val location: String?,
    val pricePerTurn: String
)

data class GamesListData(
    val items: List<GameItem>,
    val total: Long
)

data class GamesResponse(
    val success: Boolean,
    val message: String,
    val data: GamesListData?
)

// Use Game
data class UseGameRequest(
    val cardUid: String,
    val terminalId: String? = null
)

data class UseGameData(
    val logId: String,
    val gameId: String,
    val userId: String,
    val cardId: String,
    val ticketId: String,
    val remainingTurns: Int,
    val ticketStatus: String,
    val playedAt: String
)

data class UseGameResponse(
    val success: Boolean,
    val message: String,
    val data: UseGameData?
)
