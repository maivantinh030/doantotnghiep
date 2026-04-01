package com.example.appcongvien.data.network

import com.example.appcongvien.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ===== AUTH =====
    @GET("api/auth/health")
    suspend fun healthCheck(): Response<ApiResponse<Nothing>>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthData>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthData>>

    // ===== USER =====
    @GET("api/user/profile")
    suspend fun getUserProfile(): Response<ApiResponse<UserDTO>>

    @PUT("api/user/profile")
    suspend fun updateProfile(@Body request: Map<String, String?>): Response<ApiResponse<UserDTO>>

    @POST("api/user/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Nothing>>

    // ===== GAMES =====
    @GET("api/games")
    suspend fun getGames(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<ApiResponse<PaginatedData<GameDTO>>>

    @GET("api/games/featured")
    suspend fun getFeaturedGames(@Query("limit") limit: Int = 5): Response<ApiResponse<List<GameDTO>>>

    @GET("api/games/categories")
    suspend fun getCategories(): Response<ApiResponse<List<String>>>

    @GET("api/games/{gameId}")
    suspend fun getGameDetail(@Path("gameId") gameId: String): Response<ApiResponse<GameDTO>>

    @GET("api/games/{gameId}/reviews")
    suspend fun getGameReviews(
        @Path("gameId") gameId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PaginatedData<GameReviewDTO>>>

    @POST("api/reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<ApiResponse<GameReviewDTO>>

    @PUT("api/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: String,
        @Body request: UpdateReviewRequest
    ): Response<ApiResponse<GameReviewDTO>>

    @GET("api/reviews/my-review")
    suspend fun getMyReview(@Query("gameId") gameId: String): Response<ApiResponse<GameReviewDTO?>>

    @DELETE("api/reviews/{reviewId}")
    suspend fun deleteReview(@Path("reviewId") reviewId: String): Response<ApiResponse<Nothing>>

    // ===== CARDS =====
    @GET("api/cards")
    suspend fun getMyCards(): Response<ApiResponse<List<CardDTO>>>

    @POST("api/cards/{cardId}/block")
    suspend fun blockCard(
        @Path("cardId") cardId: String,
        @Body request: BlockCardRequest
    ): Response<ApiResponse<CardDTO>>

    // ===== CARD REQUESTS =====
    @POST("api/card-requests")
    suspend fun createCardRequest(
        @Body request: CreateCardRequestRequest
    ): Response<ApiResponse<CardRequestDTO>>

    @GET("api/card-requests/my")
    suspend fun getMyCardRequests(): Response<ApiResponse<List<CardRequestDTO>>>

    // ===== WALLET =====
    @GET("api/wallet/balance")
    suspend fun getWalletBalance(): Response<ApiResponse<WalletBalanceDTO>>

    @POST("api/wallet/topup")
    suspend fun topUp(@Body request: TopUpRequest): Response<ApiResponse<TransactionDTO>>

    @GET("api/wallet/transactions")
    suspend fun getTransactions(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10,
        @Query("type") type: String? = null
    ): Response<ApiResponse<PaginatedData<TransactionDTO>>>

    @GET("api/wallet/payments")
    suspend fun getPayments(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PaginatedData<PaymentRecordDTO>>>

    // ===== NOTIFICATIONS =====
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PaginatedData<NotificationDTO>>>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): Response<ApiResponse<UnreadCountDTO>>

    @POST("api/notifications/device-token")
    suspend fun registerPushToken(@Body request: RegisterPushTokenRequest): Response<ApiResponse<PushTokenDTO>>

    @POST("api/notifications/device-token/unregister")
    suspend fun unregisterPushToken(
        @Body request: UnregisterPushTokenRequest,
        @Header("Authorization") authorization: String? = null
    ): Response<ApiResponse<Nothing>>

    @POST("api/notifications/{notificationId}/read")
    suspend fun markAsRead(@Path("notificationId") notificationId: String): Response<ApiResponse<Nothing>>

    @POST("api/notifications/read-all")
    suspend fun markAllAsRead(): Response<ApiResponse<Nothing>>

    @DELETE("api/notifications/{notificationId}")
    suspend fun deleteNotification(@Path("notificationId") notificationId: String): Response<ApiResponse<Nothing>>

    // ===== SUPPORT =====
    @GET("api/support/messages")
    suspend fun getSupportMessages(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50
    ): Response<ApiResponse<PaginatedData<SupportMessageDTO>>>

    @POST("api/support/messages")
    suspend fun sendSupportMessage(@Body request: SendMessageRequest): Response<ApiResponse<SupportMessageDTO>>

    // ===== ANNOUNCEMENTS (Carousel) =====
    @GET("api/announcements")
    suspend fun getAnnouncements(): Response<ApiResponse<List<AnnouncementDTO>>>
}
