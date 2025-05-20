package com.odemirel.config

import io.ktor.server.config.*


enum class AppEnvironment {
    DEVELOPMENT, TEST, PRODUCTION;

    companion object {
        fun fromString(value: String): AppEnvironment = when (value.lowercase()) {
            "development", "dev" -> DEVELOPMENT
            "test" -> TEST
            "production", "prod" -> PRODUCTION
            else -> DEVELOPMENT
        }
    }
}

object EnvironmentManager {
    private var initialized = false

    lateinit var currentEnvironment: AppEnvironment
        private set

    fun initialize(config: ApplicationConfig) {
        if (initialized) return

        val envString = config.propertyOrNull("ktor.application.environment")?.getString() ?: "development"
        currentEnvironment = AppEnvironment.fromString(envString)
        initialized = true
    }

    fun isDevelopment() = currentEnvironment == AppEnvironment.DEVELOPMENT
    fun isTest() = currentEnvironment == AppEnvironment.TEST
    fun isProduction() = currentEnvironment == AppEnvironment.PRODUCTION
}

/**
 * Extension functions for ApplicationConfig related to environment
 */
fun ApplicationConfig.getEnvironment(): AppEnvironment {
    EnvironmentManager.initialize(this)
    return EnvironmentManager.currentEnvironment
}

fun ApplicationConfig.isDevelopment() = EnvironmentManager.isDevelopment()
fun ApplicationConfig.isTest() = EnvironmentManager.isTest()
fun ApplicationConfig.isProduction() = EnvironmentManager.isProduction() 