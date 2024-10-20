package com.yappeer.domain.onboarding.repository

import com.yappeer.domain.onboarding.model.User
import com.yappeer.domain.onboarding.model.result.RegistrationResult
import com.yappeer.domain.onboarding.model.value.Email
import com.yappeer.domain.onboarding.model.value.Password
import com.yappeer.domain.onboarding.model.value.Username
import kotlinx.datetime.Instant
import java.util.UUID

interface OnboardingRepository {

    fun register(
        username: Username,
        hashedPassword: Password,
        email: Email,
    ): RegistrationResult

    fun findPassword(email: Email): Password?

    fun findUser(userId: UUID): User?

    fun findUser(email: Email): User?

    fun updateLastLogin(userId: UUID, instant: Instant)
}
