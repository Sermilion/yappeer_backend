package com.yappeer.data.onboarding.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.yappeer.domain.config.EnvironmentConfigProvider
import com.yappeer.domain.onboarding.model.value.Password
import com.yappeer.domain.onboarding.security.UserAuthenticationService
import com.yappeer.domain.onboarding.security.UserAuthenticationService.Companion.CLAIM_TOKEN_ID
import com.yappeer.domain.onboarding.security.UserAuthenticationService.Companion.CLAIM_USER_ID
import de.mkammerer.argon2.Argon2Factory
import java.util.Date
import java.util.UUID

class YappeerUserAuthenticationService(
    private val configProvider: EnvironmentConfigProvider,
) : UserAuthenticationService {

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

    override fun generateAccessToken(userId: UUID): String {
        val config = configProvider.provideJwtConfig()

        val token = JWT.create()
            .withClaim(CLAIM_USER_ID, userId.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + TOKEN_DURATION))
            .sign(Algorithm.HMAC256(config.secret))
        return token
    }

    override fun generateRefreshToken(userId: UUID): String {
        val config = configProvider.provideJwtConfig()
        val tokenId = UUID.randomUUID().toString()
        return JWT.create()
            .withClaim(CLAIM_USER_ID, userId.toString())
            .withClaim(CLAIM_TOKEN_ID, tokenId)
            .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_TOKEN_DURATION))
            .sign(Algorithm.HMAC256(config.secret))
    }

    companion object {
        private const val ITERATIONS = 10
        private const val MEMORY_KB = 65536 // 64 MB
        private const val PARALLELISM = 1
        private const val TOKEN_DURATION = 60000L
        private const val REFRESH_TOKEN_DURATION = 3600000L
    }
}
