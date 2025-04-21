package com.yappeer.data.onboarding.security

import com.yappeer.domain.onboarding.security.JwtTokenService
import org.slf4j.LoggerFactory

class YappeerJwtTokenService : JwtTokenService {

    private val logger = LoggerFactory.getLogger(YappeerJwtTokenService::class.simpleName)

    private var cachedSecret: String? = null

    override fun loadJwtSecret(): String {
        // Return cached secret if available
        cachedSecret?.let { return it }

        // Try to get from environment variable
        val secretFromEnv = System.getenv(JWT_SECRET_KEY)?.also {
            cachedSecret = it
        }

        // If environment variable is not set, use a default for development
        return secretFromEnv ?: DEFAULT_DEV_SECRET.also {
            logger.warn(
                "WARNING: Using default development JWT secret. " +
                    "Set $JWT_SECRET_KEY environment variable for production.",
            )
            cachedSecret = it
        }
    }

    private companion object {
        const val JWT_SECRET_KEY = "jwtsecret"
        const val DEFAULT_DEV_SECRET = "yappeer_dev_secret_key_do_not_use_in_production_2025"
    }
}
