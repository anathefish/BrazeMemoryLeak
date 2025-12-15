package com.example.brazememoryleak.api

import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * REST API client setup similar to Fishbrain's RutilusServiceGateway.
 * Provides Retrofit service instances with proper OkHttp configuration.
 */
@Singleton
class RestApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val retrofit: Retrofit
) {

    /**
     * Creates a Retrofit service instance for the given interface.
     */
    fun <T> createService(serviceInterface: Class<T>): T {
        return retrofit.create(serviceInterface)
    }

    /**
     * Creates a Retrofit service with a custom base URL.
     */
    fun <T> createService(serviceInterface: Class<T>, baseUrl: String): T {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(serviceInterface)
    }

    companion object {
        private const val READ_TIMEOUT = 30L
        private const val WRITE_TIMEOUT = 30L
        private const val CONNECT_TIMEOUT = 30L

        /**
         * Creates a configured OkHttpClient similar to Fishbrain's setup.
         */
        fun createOkHttpClient(
            loggingInterceptor: HttpLoggingInterceptor,
            cache: Cache? = null
        ): OkHttpClient {
            return OkHttpClient.Builder().apply {
                addInterceptor(loggingInterceptor)
                connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                cache?.let { cache(it) }
            }.build()
        }

        /**
         * Creates a configured Retrofit instance.
         */
        fun createRetrofit(
            okHttpClient: OkHttpClient,
            baseUrl: String
        ): Retrofit {
            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}
