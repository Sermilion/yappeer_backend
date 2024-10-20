package com.yappeer.domain.onboarding.datasorce

import com.yappeer.domain.onboarding.model.User
import com.yappeer.domain.onboarding.model.result.SqlRegistrationResult
import com.yappeer.domain.onboarding.model.value.Email
import com.yappeer.domain.onboarding.model.value.Password
import kotlinx.datetime.Instant
import java.util.UUID

interface UserDataSource {
    fun createUser(
        username: String,
        email: String,
        hashedPassword: String,
    ): SqlRegistrationResult

    fun findPassword(email: Email): Password?
    fun findUser(userId: UUID): User?
    fun findUser(email: Email): User?
    fun updateLastLogin(userId: UUID, instant: Instant)
}