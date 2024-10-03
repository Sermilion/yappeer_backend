package com.sermilion.data.onboarding.model

import java.util.UUID

data class UserDataModel(
    val id: UUID,
    val username: String,
    val email: String,
    val avatar: String?,
)
