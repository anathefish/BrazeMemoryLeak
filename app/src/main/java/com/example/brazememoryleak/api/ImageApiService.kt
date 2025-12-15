package com.example.brazememoryleak.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API service for fetching images from Lorem Picsum (picsum.photos)
 * Free public API that provides random images
 */
interface ImageApiService {

    @GET("v2/list")
    suspend fun getImages(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20
    ): List<PicsumImage>
}

data class PicsumImage(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
    val download_url: String
) {
    /**
     * Get a resized version of the image
     */
    fun getResizedUrl(width: Int, height: Int): String {
        return "https://picsum.photos/id/$id/$width/$height"
    }
}
