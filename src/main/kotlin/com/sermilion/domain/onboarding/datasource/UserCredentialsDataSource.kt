package com.sermilion.domain.onboarding.datasource

import com.sermilion.data.onboarding.db.model.result.SQLResult

interface UserCredentialsDataSource {
    fun createUser(
        username: String,
        email: String,
        salt: String,
        hashedPassword: String,
    ): SQLResult
}
