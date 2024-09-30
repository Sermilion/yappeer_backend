package com.sermilion.data.onboarding.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.sermilion.domain.config.EnvironmentConfigProvider
import com.sermilion.domain.onboarding.model.value.Password
import com.sermilion.domain.onboarding.model.value.Username
import com.sermilion.domain.onboarding.security.SecurityService
import de.mkammerer.argon2.Argon2Factory
import java.util.Date
import java.util.UUID

class OilaSecurityService(
    private val configProvider: EnvironmentConfigProvider,
) : SecurityService {

    private val argon2 = Argon2Factory.create()

    override fun hashPassword(password: Password): Password {
        return Password(
            argon2.hash(
                ITERATIONS,
                MEMORY_KB,
                PARALLELISM,
                password.value.toCharArray(),
            ),
        )
    }

    override fun verifyPassword(password: Password, hashedPassword: Password): Boolean {
        try {
            return argon2.verify(hashedPassword.value, password.value)
        } finally {
            argon2.wipeArray(password.value.toCharArray())
        }
    }

    override fun generateAccessToken(username: Username): String {
        val config = configProvider.provideJwtConfig()

        val token = JWT.create()
            .withClaim(TOKEN_USERNAME, username.value)
            .withExpiresAt(Date(System.currentTimeMillis() + TOKEN_DURATION))
            .sign(Algorithm.HMAC256(config.secret))
        return token
    }

    override fun generateRefreshToken(username: Username): String {
        val config = configProvider.provideJwtConfig()
        val tokenId = UUID.randomUUID().toString()
        return JWT.create()
            .withClaim(USERNAME, username.value)
            .withClaim(TOKEN_ID, tokenId)
            .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_TOKEN_DURATION))
            .sign(Algorithm.HMAC256(config.secret))
    }

    companion object {
        private const val ITERATIONS = 10
        private const val MEMORY_KB = 65536 // 64 MB
        private const val PARALLELISM = 1
        private const val TOKEN_USERNAME = "username"
        private const val TOKEN_DURATION = 60000L
        private const val USERNAME = "username"
        private const val TOKEN_ID = "tokenId"
        private const val REFRESH_TOKEN_DURATION = 3600000L
    }
}
