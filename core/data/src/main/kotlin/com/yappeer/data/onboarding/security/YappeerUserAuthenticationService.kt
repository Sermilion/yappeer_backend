package com.yappeer.data.onboarding.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.yappeer.domain.onboarding.model.value.Email
import com.yappeer.domain.onboarding.model.value.Password
import com.yappeer.domain.onboarding.security.JwtTokenService
import com.yappeer.domain.onboarding.security.UserAuthenticationService
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.util.Date
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes

class YappeerUserAuthenticationService(
    private val jwtTokenService: JwtTokenService,
) : UserAuthenticationService {

    private val failedLoginAttempts = ConcurrentHashMap<String, FailedLoginData>()

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
        val secret = jwtTokenService.loadJwtSecret()
        val now = Date()
        val expiry = Date(now.time + ACCESS_TOKEN_EXPIRATION_MS)

        return JWT.create()
            .withClaim(UserAuthenticationService.CLAIM_USER_ID, userId.toString())
            .withClaim(UserAuthenticationService.CLAIM_TOKEN_ID, UUID.randomUUID().toString())
            .withClaim(UserAuthenticationService.CLAIM_EXPIRATION, expiry)
            .withIssuedAt(now)
            .withExpiresAt(expiry)
            .sign(Algorithm.HMAC256(secret))
    }

    override fun generateRefreshToken(userId: UUID): String {
        val secret = jwtTokenService.loadJwtSecret()
        val now = Date()
        val expiry = Date(now.time + REFRESH_TOKEN_EXPIRATION_MS)

        return JWT.create()
            .withClaim(UserAuthenticationService.CLAIM_USER_ID, userId.toString())
            .withClaim(UserAuthenticationService.CLAIM_TOKEN_ID, UUID.randomUUID().toString())
            .withClaim(UserAuthenticationService.CLAIM_EXPIRATION, expiry)
            .withIssuedAt(now)
            .withExpiresAt(expiry)
            .sign(Algorithm.HMAC256(secret))
    }

    override fun getAccessTokenExpiration(): Long {
        return ACCESS_TOKEN_EXPIRATION_SEC
    }

    override fun recordFailedLoginAttempt(email: Email) {
        val key = email.value.lowercase()
        val now = Clock.System.now()

        failedLoginAttempts.compute(key) { _, data ->
            if (data == null) {
                FailedLoginData(1, now)
            } else {
                // If the last attempt was more than LOCK_DURATION_MINUTES ago, reset the count
                if (data.lockedUntil != null && now > data.lockedUntil!!) {
                    data.attempts = 1
                    data.lockedUntil = null
                } else {
                    data.attempts++

                    // Lock the account if too many attempts
                    if (data.attempts >= UserAuthenticationService.MAX_FAILED_ATTEMPTS) {
                        data.lockedUntil = now.plus(
                            UserAuthenticationService.LOCK_DURATION_MINUTES.minutes,
                        )
                    }
                }
                data.lastFailedAt = now
                data
            }
        }
    }

    override fun clearFailedAttempts(email: Email) {
        failedLoginAttempts.remove(email.value.lowercase())
    }

    override fun isAccountLocked(email: Email): Boolean {
        val key = email.value.lowercase()
        val data = failedLoginAttempts[key] ?: return false

        val now = Clock.System.now()

        // If account is locked and lock hasn't expired
        return data.lockedUntil != null && now < data.lockedUntil!!
    }

    private data class FailedLoginData(
        var attempts: Int,
        var lastFailedAt: Instant,
        var lockedUntil: Instant? = null,
    )

    private companion object {
        const val ITERATIONS = 10
        const val HASH_LENGTH = 32
        const val SALT_LENGTH = 16
        const val MEMORY_KB = 65536 // 64 MB
        const val PARALLELISM = 1
        private const val REFRESH_TOKEN_EXPIRATION_MS = 2_592_000_000L // 30 days
        private const val ACCESS_TOKEN_EXPIRATION_MS = 3_600_000L // 1 hour
        private const val ACCESS_TOKEN_EXPIRATION_SEC = 3_600L // 1 hour in seconds
    }
}
