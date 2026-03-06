package com.example.testnfc.data.network

import com.example.testnfc.data.model.AdminAuthResponse
import com.example.testnfc.data.model.AdminLoginRequest
import com.example.testnfc.data.model.GamesResponse
import com.example.testnfc.data.model.UseGameRequest
import com.example.testnfc.data.model.UseGameResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/admin/auth/login")
    suspend fun login(@Body request: AdminLoginRequest): Response<AdminAuthResponse>

    @GET("api/games")
    suspend fun getGames(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50
    ): Response<GamesResponse>

    @POST("api/games/{gameId}/play")
    suspend fun useGame(
        @Header("Authorization") token: String,
        @Path("gameId") gameId: String,
        @Body request: UseGameRequest
    ): Response<UseGameResponse>
}
