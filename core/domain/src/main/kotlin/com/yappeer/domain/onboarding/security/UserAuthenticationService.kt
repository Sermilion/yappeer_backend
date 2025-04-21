package com.yappeer.domain.onboarding.security

import com.yappeer.domain.onboarding.model.value.Email
import com.yappeer.domain.onboarding.model.value.Password
import java.util.UUID

interface UserAuthenticationService {
    fun hashPassword(password: Password): Password
    fun verifyPassword(password: Password, hashedPassword: Password): Boolean
    fun generateAccessToken(userId: UUID): String
    fun generateRefreshToken(userId: UUID): String
    fun getAccessTokenExpiration(): Long
    fun recordFailedLoginAttempt(email: Email)
    fun clearFailedAttempts(email: Email)
    fun isAccountLocked(email: Email): Boolean

    companion object {
        const val CLAIM_USER_ID = "userId"
        const val CLAIM_TOKEN_ID = "tokenId"
        const val CLAIM_EXPIRATION = "exp"
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCK_DURATION_MINUTES = 15
    }
}
