package com.amakaflow.companion.di

import com.amakaflow.companion.BuildConfig
import com.amakaflow.companion.data.AppEnvironment
import com.amakaflow.companion.data.api.AmakaflowApi
import com.amakaflow.companion.data.api.AuthAuthenticator
import com.amakaflow.companion.data.api.AuthStateManager
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
import javax.inject.Provider
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
    @Named("mapperBaseUrl")
    fun provideMapperBaseUrl(): String = AppEnvironment.current.mapperApiUrl + "/"

    @Provides
    @Singleton
    fun provideAuthStateManager(): AuthStateManager = object : AuthStateManager {
        private var callback: (() -> Unit)? = null

        override fun markNeedsReauth() {
            callback?.invoke()
        }

        fun setCallback(onNeedsReauth: () -> Unit) {
            callback = onNeedsReauth
        }
    }

    @Provides
    @Singleton
    fun provideAuthAuthenticator(
        secureStorage: SecureStorage,
        json: Json,
        @Named("mapperBaseUrl") baseUrlProvider: Provider<String>,
        authStateManager: AuthStateManager
    ): AuthAuthenticator {
        return AuthAuthenticator(secureStorage, json, baseUrlProvider, authStateManager)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(secureStorage: SecureStorage): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()

            // Add JWT token if available
            secureStorage.getToken()?.let { token ->
                request.addHeader("Authorization", "Bearer $token")
            }

            // Add E2E test headers if configured
            if (BuildConfig.DEBUG) {
                getTestAuthSecret()?.let { secret ->
                    request.addHeader("X-Test-Auth", secret)
                    getTestUserId()?.let { userId ->
                        request.addHeader("X-Test-User-Id", userId)
                    }
                }
            }

            request.addHeader("Content-Type", "application/json")
            chain.proceed(request.build())
        }
    }

    private fun getTestAuthSecret(): String? = System.getenv("TEST_AUTH_SECRET")
    private fun getTestUserId(): String? = System.getenv("TEST_USER_ID")

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        authAuthenticator: AuthAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(authAuthenticator)
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
