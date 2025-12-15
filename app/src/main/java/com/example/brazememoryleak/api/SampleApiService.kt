package com.example.brazememoryleak.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Sample REST API service interface demonstrating Retrofit setup.
 * These endpoints are examples and won't actually work - they're just for reproducing the memory leak.
 */
interface SampleApiService {

    @GET("users/me")
    suspend fun getCurrentUser(): UserResponse

    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): UserResponse

    @GET("users")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): List<UserResponse>

    @POST("users/me/position")
    suspend fun updatePosition(@Body position: PositionRequest)

    @POST("users/me/logout")
    suspend fun logout(@Body body: Map<String, String>)
}

data class UserResponse(
    val id: Int,
    val nickname: String?,
    val email: String?,
    val avatarUrl: String?
)

data class PositionRequest(
    val latitude: Double,
    val longitude: Double
)
