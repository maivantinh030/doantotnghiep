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

    @DELETE("api/reviews/{reviewId}")
    suspend fun deleteReview(@Path("reviewId") reviewId: String): Response<ApiResponse<Nothing>>

    // ===== CARDS =====
    @GET("api/cards")
    suspend fun getMyCards(): Response<ApiResponse<List<CardDTO>>>

    @POST("api/cards/link")
    suspend fun linkCard(@Body request: LinkCardRequest): Response<ApiResponse<CardDTO>>

    @PUT("api/cards/{cardId}")
    suspend fun updateCard(
        @Path("cardId") cardId: String,
        @Body request: UpdateCardRequest
    ): Response<ApiResponse<CardDTO>>

    @POST("api/cards/{cardId}/block")
    suspend fun blockCard(
        @Path("cardId") cardId: String,
        @Body request: BlockCardRequest
    ): Response<ApiResponse<CardDTO>>

    @POST("api/cards/{cardId}/unblock")
    suspend fun unblockCard(@Path("cardId") cardId: String): Response<ApiResponse<CardDTO>>

    @DELETE("api/cards/{cardId}/unlink")
    suspend fun unlinkCard(@Path("cardId") cardId: String): Response<ApiResponse<Nothing>>

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

    // ===== VOUCHERS =====
    @GET("api/vouchers")
    suspend fun getVouchers(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PaginatedData<VoucherDTO>>>

    @GET("api/vouchers/code/{code}")
    suspend fun getVoucherByCode(@Path("code") code: String): Response<ApiResponse<VoucherDTO>>

    @POST("api/vouchers/{voucherId}/claim")
    suspend fun claimVoucher(@Path("voucherId") voucherId: String): Response<ApiResponse<UserVoucherDTO>>

    @GET("api/vouchers/my-vouchers")
    suspend fun getMyVouchers(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PaginatedData<UserVoucherDTO>>>

    // ===== ORDERS =====
    @POST("api/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<ApiResponse<OrderDTO>>

    @GET("api/orders")
    suspend fun getOrders(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PaginatedData<OrderDTO>>>

    @GET("api/orders/{orderId}")
    suspend fun getOrderDetail(@Path("orderId") orderId: String): Response<ApiResponse<OrderDTO>>

    @POST("api/orders/{orderId}/cancel")
    suspend fun cancelOrder(
        @Path("orderId") orderId: String,
        @Body request: CancelOrderRequest
    ): Response<ApiResponse<OrderDTO>>

    @GET("api/tickets")
    suspend fun getMyTickets(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PaginatedData<TicketDTO>>>

    // ===== NOTIFICATIONS =====
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PaginatedData<NotificationDTO>>>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): Response<ApiResponse<UnreadCountDTO>>

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
}
