package com.sermilion.domain.onboarding.datasource

import com.sermilion.data.onboarding.db.model.result.SqlRegistrationResult
import com.sermilion.domain.onboarding.model.value.Password

interface UserCredentialsDataSource {
    fun createUser(
        username: String,
        email: String,
        hashedPassword: String,
    ): SqlRegistrationResult

    fun findPassword(username: String): Password?
}
