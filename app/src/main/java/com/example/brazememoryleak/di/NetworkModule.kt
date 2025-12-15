package com.example.brazememoryleak.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.example.brazememoryleak.api.GraphQLApiClient
import com.example.brazememoryleak.api.ImageApiService
import com.example.brazememoryleak.api.RestApiClient
import com.example.brazememoryleak.api.SampleApiService
import com.example.brazememoryleak.api.SpaceXApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.example.com/"
    private const val PICSUM_BASE_URL = "https://picsum.photos/"
    private const val GRAPHQL_URL = "https://graphql.example.com/graphql"
    private const val SPACEX_GRAPHQL_URL = "https://spacex-production.up.railway.app/graphql"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApolloClient(okHttpClient: OkHttpClient): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(GRAPHQL_URL)
            .okHttpClient(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideRestApiClient(
        okHttpClient: OkHttpClient,
        retrofit: Retrofit
    ): RestApiClient {
        return RestApiClient(okHttpClient, retrofit)
    }

    @Provides
    @Singleton
    fun provideGraphQLApiClient(apolloClient: ApolloClient): GraphQLApiClient {
        return GraphQLApiClient(apolloClient)
    }

    @Provides
    @Singleton
    fun provideSampleApiService(retrofit: Retrofit): SampleApiService {
        return retrofit.create(SampleApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideImageApiService(okHttpClient: OkHttpClient): ImageApiService {
        return Retrofit.Builder()
            .baseUrl(PICSUM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImageApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSpaceXApolloClient(okHttpClient: OkHttpClient): SpaceXApiClient {
        val apolloClient = ApolloClient.Builder()
            .serverUrl(SPACEX_GRAPHQL_URL)
            .okHttpClient(okHttpClient)
            .build()
        return SpaceXApiClient(apolloClient)
    }
}
