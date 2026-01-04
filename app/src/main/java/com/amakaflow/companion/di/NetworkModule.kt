package com.amakaflow.companion.di

import com.amakaflow.companion.data.AppEnvironment
import com.amakaflow.companion.data.api.AmakaflowApi
import com.amakaflow.companion.data.api.IngestorApi
import com.amakaflow.companion.data.local.SecureStorage
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(secureStorage: SecureStorage): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
            secureStorage.getToken()?.let { token ->
                request.addHeader("Authorization", "Bearer $token")
            }
            request.addHeader("Content-Type", "application/json")
            chain.proceed(request.build())
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("mapper")
    fun provideMapperRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(AppEnvironment.current.mapperApiUrl + "/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @Named("ingestor")
    fun provideIngestorRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(AppEnvironment.current.ingestorApiUrl + "/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAmakaflowApi(@Named("mapper") retrofit: Retrofit): AmakaflowApi {
        return retrofit.create(AmakaflowApi::class.java)
    }

    @Provides
    @Singleton
    fun provideIngestorApi(@Named("ingestor") retrofit: Retrofit): IngestorApi {
        return retrofit.create(IngestorApi::class.java)
    }
}
