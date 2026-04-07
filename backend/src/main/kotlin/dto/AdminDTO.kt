package com.park.dto

import com.park.entities.Admin
import kotlinx.serialization.Serializable

@Serializable
data class AdminLoginRequest(
    val phoneNumber: String,
    val password: String
)

@Serializable
data class CreateAdminRequest(
    val phoneNumber: String,
    val password: String,
    val fullName: String,
    val employeeCode: String? = null
)

@Serializable
data class AdminInfo(
    val adminId: String,
    val accountId: String,
    val phoneNumber: String,       // phoneNumber dùng làm username, khớp với desktop AdminProfile.username
    val fullName: String,
    val employeeCode: String?,
    val role: String = "ADMIN"
) {
    companion object {
        fun fromEntity(admin: Admin, phoneNumber: String): AdminInfo {
            return AdminInfo(
                adminId = admin.adminId,
                accountId = admin.accountId,
                phoneNumber = phoneNumber,
                fullName = admin.fullName,
                employeeCode = admin.employeeCode
            )
        }
    }
}

@Serializable
data class AdminAuthResponse(
    val success: Boolean,
    val message: String,
    val data: AdminAuthData? = null
)

@Serializable
data class AdminAuthData(
    val token: String,
    val admin: AdminInfo         // đổi từ "admin" → "user" để khớp với desktop AuthData.user
)

@Serializable
data class AdminUserDTO(
    val userId: String,
    val accountId: String,
    val phoneNumber: String,
    val fullName: String?,
    val email: String?,
    val currentBalance: String,
    val accountStatus: String,
    val createdAt: String
)

@Serializable
data class AdjustBalanceRequest(
    val amount: Double,
    val description: String? = null
)

@Serializable
data class SendNotificationRequest(
    val title: String,
    val message: String,
    val targetType: String = "ALL", // ALL, GOLD, SILVER, BRONZE, PLATINUM, etc.
    val targetUserId: String? = null // specific user if targetType = "USER"
)

@Serializable
data class AdminReplyRequest(
    val userId: String,
    val content: String
)

@Serializable
data class AdminTransactionDTO(
    val transactionId: String,
    val userId: String,
    val amount: String,
    val balanceBefore: String,
    val balanceAfter: String,
    val type: String,
    val referenceType: String?,
    val referenceId: String?,
    val description: String?,
    val createdAt: String,
    val createdBy: String?
)

@Serializable
data class AdminSentNotificationDTO(
    val notificationId: String,
    val title: String,
    val message: String,
    val targetType: String?,
    val createdAt: String
)

@Serializable
data class AdminSupportMessageDTO(
    val messageId: String,
    val userId: String,
    val userName: String? = null,
    val content: String,
    val isFromAdmin: Boolean,
    val createdAt: String? = null
)

@Serializable
data class AdminSupportMessagesResponse(
    val items: List<AdminSupportMessageDTO>,
    val total: Int,
    val page: Int,
    val size: Int,
    val totalPages: Int
)

@Serializable
data class AdminSupportApiResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: AdminSupportMessagesResponse? = null
)

@Serializable
data class RevenueChartResponse(
    val labels: List<String>,
    val values: List<Double>,
    val totalInPeriod: Double
)

@Serializable
data class AdminStatisticsFilterOptionDTO(
    val gameId: String,
    val name: String,
    val area: String? = null
)

@Serializable
data class AdminStatisticsFiltersDTO(
    val games: List<AdminStatisticsFilterOptionDTO>,
    val areas: List<String>,
    val ticketTypes: List<String>,
    val statuses: List<String>,
    val groupings: List<String>
)

@Serializable
data class AdminStatisticsTrendDTO(
    val labels: List<String>,
    val revenueValues: List<Double>,
    val playerValues: List<Int>,
    val totalRevenue: Double,
    val totalPlayers: Int
)

@Serializable
data class AdminStatisticsGameItemDTO(
    val gameId: String,
    val name: String,
    val area: String? = null,
    val plays: Int,
    val players: Int,
    val revenue: Double,
    val ticketPrice: Double,
    val revenuePerPlay: Double,
    val contributionPercent: Double,
    val status: String
)

@Serializable
data class AdminStatisticsTopItemDTO(
    val gameId: String,
    val name: String,
    val players: Int,
    val revenue: Double
)

@Serializable
data class AdminStatisticsCardStatusDTO(
    val active: Int,
    val available: Int,
    val blocked: Int
)

@Serializable
data class AdminStatisticsSummaryDTO(
    val totalGames: Int,
    val totalPlays: Int,
    val totalPlayers: Int,
    val totalRevenue: Double
)

@Serializable
data class AdminStatisticsGamesResponseDTO(
    val items: List<AdminStatisticsGameItemDTO>,
    val summary: AdminStatisticsSummaryDTO,
    val topRevenue: List<AdminStatisticsTopItemDTO>,
    val lowPlayers: List<AdminStatisticsTopItemDTO>,
    val cardStatus: AdminStatisticsCardStatusDTO
)

@Serializable
data class AdminStatisticsTableResponseDTO(
    val items: List<AdminStatisticsGameItemDTO>,
    val total: Long,
    val page: Int,
    val size: Int,
    val totalPages: Long,
    val summary: AdminStatisticsSummaryDTO
)

@Serializable
data class GamePerformanceDTO(
    val gameId: String,
    val name: String,
    val totalPlays: Int,
    val estimatedRevenue: Double
)
