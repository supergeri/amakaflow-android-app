package com.amakaflow.companion.data

import com.amakaflow.companion.BuildConfig

/**
 * App environment configuration
 */
enum class AppEnvironment {
    PRODUCTION,
    STAGING,
    DEVELOPMENT,
    LOCALHOST;

    val mapperApiUrl: String
        get() = when (this) {
            PRODUCTION -> BuildConfig.MAPPER_API_URL_PROD
            STAGING -> BuildConfig.MAPPER_API_URL_STAGING
            DEVELOPMENT -> BuildConfig.MAPPER_API_URL_DEV
            LOCALHOST -> BuildConfig.MAPPER_API_URL_LOCALHOST
        }

    val ingestorApiUrl: String
        get() = when (this) {
            PRODUCTION -> BuildConfig.INGESTOR_API_URL_PROD
            STAGING -> BuildConfig.INGESTOR_API_URL_STAGING
            DEVELOPMENT -> BuildConfig.INGESTOR_API_URL_DEV
            LOCALHOST -> BuildConfig.INGESTOR_API_URL_LOCALHOST
        }

    val displayName: String
        get() = when (this) {
            PRODUCTION -> "Production"
            STAGING -> "Staging"
            DEVELOPMENT -> "Development"
            LOCALHOST -> "Localhost"
        }

    companion object {
        var current: AppEnvironment = PRODUCTION
    }
}
