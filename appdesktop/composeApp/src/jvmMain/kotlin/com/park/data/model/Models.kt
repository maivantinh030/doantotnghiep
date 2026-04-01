package com.park.data.model

import kotlinx.serialization.Serializable

// ===== Common =====
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

@Serializable
data class PaginatedData<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val size: Int = 10,
    val totalPages: Int = 1
)

// ===== Auth =====
@Serializable
data class LoginRequest(
    val phoneNumber: String,
    val password: String
)

@Serializable
data class AuthData(
    val token: String,
    val admin: AdminProfile
)

@Serializable
data class AdminProfile(
    val adminId: String,
    val accountId: String,
    val phoneNumber: String,
    val fullName: String,
    val employeeCode: String?,
    val role: String = "ADMIN"
)

// ===== User Management =====
@Serializable
data class UserDTO(
    val userId: String,
    val phoneNumber: String,
    val fullName: String,
    val email: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val avatarUrl: String? = null,
    val currentBalance: String = "0",
    val status: String = "ACTIVE",
    val createdAt: String? = null
)

@Serializable
data class AdjustBalanceRequest(
    val amount: Double,
    val reason: String
)

// ===== Game Management =====
@Serializable
data class GameDTO(
    val gameId: String,
    val name: String,
    val description: String? = null,
    val shortDescription: String? = null,
    val category: String = "",
    val pricePerTurn: String = "0",
    val durationMinutes: Int? = null,
    val location: String? = null,
    val thumbnailUrl: String? = null,
    val ageRequired: Int? = null,
    val heightRequired: Int? = null,
    val maxCapacity: Int? = null,
    val riskLevel: Int? = null,
    val isFeatured: Boolean = false,
    val status: String = "ACTIVE",
    val avgRating: Double? = null,
    val totalReviews: Int? = null
)

@Serializable
data class CreateGameRequest(
    val name: String,
    val description: String? = null,
    val shortDescription: String? = null,
    val category: String,
    val pricePerTurn: Double,
    val durationMinutes: Int? = null,
    val location: String? = null,
    val thumbnailUrl: String? = null,
    val ageRequired: Int? = null,
    val heightRequired: Int? = null,
    val maxCapacity: Int? = null,
    val riskLevel: Int? = null,
    val isFeatured: Boolean = false
)

@Serializable
data class UpdateGameRequest(
    val name: String? = null,
    val description: String? = null,
    val shortDescription: String? = null,
    val category: String? = null,
    val pricePerTurn: Double? = null,
    val durationMinutes: Int? = null,
    val location: String? = null,
    val thumbnailUrl: String? = null,
    val ageRequired: Int? = null,
    val heightRequired: Int? = null,
    val maxCapacity: Int? = null,
    val riskLevel: Int? = null,
    val isFeatured: Boolean? = null,
    val status: String? = null
)

// ===== Cards / Smart Card =====
@Serializable
data class CardDTO(
    val cardId: String,
    val cardName: String? = null,
    val userId: String? = null,
    val status: String,                    // AVAILABLE | ACTIVE | BLOCKED
    val depositAmount: String = "0",
    val depositStatus: String = "NONE",   // NONE | PAID | REFUNDED | FORFEITED
    val issuedAt: String? = null,
    val blockedAt: String? = null,
    val blockedReason: String? = null,
    val lastUsedAt: String? = null,
    val createdAt: String
)

@Serializable
data class RegisterCardRequest(
    val cardId: String,
    val cardName: String? = null
)

@Serializable
data class IssueCardRequest(
    val cardId: String,
    val userId: String,
    val cardName: String? = null,
    val depositAmount: String = "0"
)

@Serializable
data class BlockCardRequest(
    val reason: String? = null
)

// ===== Card Requests (from mobile app) =====
@Serializable
data class CardRequestDTO(
    val requestId: String,
    val userId: String,
    val status: String,                   // PENDING | APPROVED | REJECTED | COMPLETED
    val depositPaidOnline: Boolean = false,
    val depositAmount: String = "0",
    val note: String? = null,
    val approvedBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class ApproveCardRequestDTO(
    val approved: Boolean,
    val note: String? = null
)

// ===== Notifications =====
@Serializable
data class SendNotificationRequest(
    val title: String,
    val message: String,
    val targetType: String = "ALL",
    val targetUserId: String? = null
)

@Serializable
data class SendNotificationResponse(
    val broadcastId: String,
    val sentCount: Int,
    val targetType: String
)

@Serializable
data class NotificationDTO(
    val notificationId: String,
    val userId: String? = null,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: String? = null
)

@Serializable
data class AdminSentNotificationDTO(
    val notificationId: String,
    val title: String,
    val message: String,
    val targetType: String?,
    val createdAt: String
)

// ===== Announcements (Carousel) =====
@Serializable
data class AnnouncementDTO(
    val announcementId: String,
    val title: String,
    val description: String? = null,
    val imageUrl: String,
    val linkType: String? = null,
    val linkValue: String? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateAnnouncementRequest(
    val title: String,
    val description: String? = null,
    val imageUrl: String,
    val linkType: String? = null,
    val linkValue: String? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

@Serializable
data class UpdateAnnouncementRequest(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val linkType: String? = null,
    val linkValue: String? = null,
    val isActive: Boolean? = null,
    val sortOrder: Int? = null
)

// ===== Support =====
@Serializable
data class SupportMessageDTO(
    val messageId: String,
    val userId: String,
    val userName: String? = null,
    val content: String,
    val isFromAdmin: Boolean = false,
    val createdAt: String? = null
)

@Serializable
data class SendSupportReplyRequest(
    val userId: String,
    val content: String
)

// ===== Dashboard Stats =====
@Serializable
data class DashboardStats(
    val totalUsers: Int = 0,
    val totalGames: Int = 0,
    val activeCards: Int = 0,
    val availableCards: Int = 0,
    val blockedCards: Int = 0,
    val totalTopUpRevenue: Double = 0.0
)

// ===== Revenue Chart =====
@Serializable
data class RevenueChartData(
    val labels: List<String> = emptyList(),
    val values: List<Double> = emptyList(),
    val totalInPeriod: Double = 0.0
)

// ===== Wallet / Finance =====
@Serializable
data class TransactionDTO(
    val transactionId: String,
    val userId: String,
    val userName: String? = null,
    val type: String,
    val amount: String,
    val description: String? = null,
    val createdAt: String? = null
)
