package com.example.brazememoryleak.api

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Mutation
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.network.okHttpClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GraphQL API client setup similar to Fishbrain's GraphQLServiceGateway and GraphQlApi.
 * Provides Apollo client configuration with OkHttp integration.
 */
@Singleton
class GraphQLApiClient @Inject constructor(
    private val apolloClient: ApolloClient
) {

    /**
     * Execute a GraphQL query.
     * Similar to Fishbrain's GraphQlApi.query() method.
     */
    suspend fun <D : Query.Data> query(query: Query<D>): Result<D> {
        return try {
            val response = apolloClient.query(query).execute()
            when {
                response.hasErrors() -> {
                    val errorMessage = response.errors?.firstOrNull()?.message ?: "Unknown GraphQL error"
                    Result.failure(GraphQLException(errorMessage))
                }
                response.data != null -> {
                    Result.success(response.data!!)
                }
                else -> {
                    Result.failure(GraphQLException("No data returned"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Execute a GraphQL mutation.
     * Similar to Fishbrain's GraphQlApi.mutate() method.
     */
    suspend fun <D : Mutation.Data> mutate(mutation: Mutation<D>): Result<D> {
        return try {
            val response = apolloClient.mutation(mutation).execute()
            when {
                response.hasErrors() -> {
                    val errorMessage = response.errors?.firstOrNull()?.message ?: "Unknown GraphQL error"
                    Result.failure(GraphQLException(errorMessage))
                }
                response.data != null -> {
                    Result.success(response.data!!)
                }
                else -> {
                    Result.failure(GraphQLException("No data returned"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val READ_TIMEOUT = 30L
        private const val WRITE_TIMEOUT = 30L
        private const val CONNECT_TIMEOUT = 30L

        /**
         * Creates a configured ApolloClient similar to Fishbrain's GraphQLServiceGateway.
         */
        fun createApolloClient(
            serverUrl: String,
            okHttpClient: OkHttpClient
        ): ApolloClient {
            return ApolloClient.Builder()
                .serverUrl(serverUrl)
                .okHttpClient(okHttpClient)
                .build()
        }

        /**
         * Creates a configured OkHttpClient for GraphQL requests.
         * Similar to Fishbrain's GraphQLServiceGateway.getOkHttpBuilder()
         */
        fun createOkHttpClient(
            loggingInterceptor: HttpLoggingInterceptor
        ): OkHttpClient {
            return OkHttpClient.Builder().apply {
                addInterceptor(loggingInterceptor)
                connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            }.build()
        }
    }
}

class GraphQLException(message: String) : Exception(message)
