package com.sermilion.domain.onboarding.model.feature

import java.util.UUID

data class Profile(
    val id: UUID,
    val username: String,
    val email: String,
    val avatar: String?,
)
