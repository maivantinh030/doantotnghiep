package com.park.data.model

import kotlinx.serialization.Serializable
import java.time.Instant

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
    val loyaltyPoints: Int = 0,
    val membershipLevel: String = "BRONZE",
    val referralCode: String? = null,
    val status: String = "ACTIVE",
    val createdAt: String? = null
)

@Serializable
data class AdjustBalanceRequest(
    val amount: Double,
    val reason: String
)

@Serializable
data class UpdateMembershipRequest(
    val membershipLevel: String
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

// ===== Voucher Management =====
@Serializable
data class VoucherDTO(
    val voucherId: String,
    val code: String,
    val title: String,
    val description: String? = null,
    val discountType: String = "PERCENTAGE",
    val discountValue: String = "0",
    val maxDiscount: String? = null,
    val minOrderValue: String? = null,
    val usageLimit: Int? = null,
    val usedCount: Int = 0,
    val perUserLimit: Int? = null,
    val applicableGames: List<String>? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class CreateVoucherRequest(
    val code: String,
    val title: String,
    val description: String? = null,
    val discountType: String,
    val discountValue: Double,
    val maxDiscount: Double? = null,
    val minOrderValue: Double? = null,
    val usageLimit: Int? = null,
    val perUserLimit: Int? = null,
    val applicableGames: List<String>? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class UpdateVoucherRequest(
    val title: String? = null,
    val description: String? = null,
    val discountValue: Double? = null,
    val maxDiscount: Double? = null,
    val minOrderValue: Double? = null,
    val usageLimit: Int? = null,
    val perUserLimit: Int? = null,
    val endDate: String? = null,
    val isActive: Boolean? = null
)

// ===== Orders =====
@Serializable
data class OrderDTO(
    val orderId: String,
    val userId: String,
    val userName: String? = null,
    val totalAmount: String = "0",
    val discountAmount: String = "0",
    val finalAmount: String = "0",
    val status: String = "PENDING",
    val paymentMethod: String? = null,
    val vourcherCode: String? = null,
    val createdAt: String? = null,
    val items: List<OrderItemDTO> = emptyList()
)

@Serializable
data class OrderItemDTO(
    val itemId: String,
    val gameId: String,
    val gameName: String,
    val quantity: Int,
    val pricePerTurn: String,
    val subtotal: String
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
    val linkType: String? = null,  // GAME | VOUCHER | SCREEN | null
    val linkValue: String? = null, // gameId | voucherCode | screenName
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
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val activeVouchers: Int = 0,
    val pendingSupport: Int = 0
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

// ===== Cards / Smart Card Lookup =====
@Serializable
data class CardDTO(
    val cardId: String,
    val physicalCardUid: String? = null,
    val virtualCardUid: String? = null,
    val cardType: String,
    val userId: String? = null,
    val cardName: String? = null,
    val status: String,
    val issuedAt: String? = null,
    val blockedAt: String? = null,
    val blockedReason: String? = null,
    val lastUsedAt: String? = null,
    val createdAt: String
)

@Serializable
data class CardLookupByUidRequest(
    val cardUid: String
)

@Serializable
data class CardLookupResultDTO(
    val card: CardDTO? = null,
    val userId: String? = null
)
