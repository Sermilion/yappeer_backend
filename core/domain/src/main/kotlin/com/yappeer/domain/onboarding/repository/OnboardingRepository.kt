package com.yappeer.domain.onboarding.repository

import com.yappeer.domain.onboarding.model.User
import com.yappeer.domain.onboarding.model.UserWithPassword
import com.yappeer.domain.onboarding.model.result.RegistrationResult
import com.yappeer.domain.onboarding.model.value.Email
import com.yappeer.domain.onboarding.model.value.Password
import com.yappeer.domain.onboarding.model.value.Username
import kotlinx.datetime.Instant
import java.util.UUID

interface OnboardingRepository {

    /**
     * Registers a new user
     */
    fun register(
        username: Username,
        hashedPassword: Password,
        email: Email,
    ): RegistrationResult

    /**
     * Finds a user's password by email
     */
    fun findPassword(email: Email): Password?

    /**
     * Finds a user by ID
     */
    fun findUser(userId: UUID): User?

    /**
     * Finds a user by email
     */
    fun findUser(email: Email): User?

    /**
     * Finds a user with their password in a single query (optimization)
     */
    fun findUserWithPassword(email: Email): UserWithPassword?

    /**
     * Updates user's last login timestamp
     */
    fun updateLastLogin(userId: UUID, instant: Instant)
}
