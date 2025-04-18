package com.yappeer.data.onboarding.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.yappeer.domain.config.EnvironmentConfigProvider
import com.yappeer.domain.onboarding.model.value.Password
import com.yappeer.domain.onboarding.security.UserAuthenticationService
import com.yappeer.domain.onboarding.security.UserAuthenticationService.Companion.CLAIM_TOKEN_ID
import com.yappeer.domain.onboarding.security.UserAuthenticationService.Companion.CLAIM_USER_ID
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.util.Date
import java.util.UUID

class YappeerUserAuthenticationService(
    private val configProvider: EnvironmentConfigProvider,
) : UserAuthenticationService {

    private var argon2 = Argon2PasswordEncoder(
        SALT_LENGTH,
        HASH_LENGTH,
        PARALLELISM,
        MEMORY_KB,
        ITERATIONS,
    )

    override fun hashPassword(password: Password): Password {
        return Password(argon2.encode(password.value))
    }

    override fun verifyPassword(password: Password, hashedPassword: Password): Boolean {
        return argon2.matches(password.value, hashedPassword.value)
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

    private companion object {
        const val ITERATIONS = 10
        const val HASH_LENGTH = 32
        const val SALT_LENGTH = 16
        const val MEMORY_KB = 65536 // 64 MB
        const val PARALLELISM = 1
        const val TOKEN_DURATION = 60000 * 60 * 24 * 7L
        const val REFRESH_TOKEN_DURATION = 3600000L
    }
}
