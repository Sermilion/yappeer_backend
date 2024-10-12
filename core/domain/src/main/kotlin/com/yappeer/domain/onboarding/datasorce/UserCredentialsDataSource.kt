package com.yappeer.domain.onboarding.datasorce

import com.yappeer.domain.onboarding.model.User
import com.yappeer.domain.onboarding.model.result.SqlRegistrationResult
import com.yappeer.domain.onboarding.model.value.Password
import com.yappeer.domain.onboarding.model.value.Username

interface UserCredentialsDataSource {
    fun createUser(
        username: String,
        email: String,
        hashedPassword: String,
    ): SqlRegistrationResult

    fun findPassword(username: String): Password?
    fun findUser(username: Username): User?
}
