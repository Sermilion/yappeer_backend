package com.sermilion.domain.onboarding.datasource

import com.sermilion.data.onboarding.db.model.result.SqlResult

interface UserCredentialsDataSource {
    fun createUser(
        username: String,
        email: String,
        hashedPassword: String,
    ): SqlResult
}
