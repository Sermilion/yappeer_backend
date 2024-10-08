package com.sermilion.domain.onboarding.datasorce

import com.sermilion.domain.onboarding.model.User
import com.sermilion.domain.onboarding.model.result.SqlRegistrationResult
import com.sermilion.domain.onboarding.model.value.Password

interface UserCredentialsDataSource {
    fun createUser(
        username: String,
        email: String,
        hashedPassword: String,
    ): SqlRegistrationResult

    fun findPassword(username: String): Password?
    fun findUser(username: String): User?
}
