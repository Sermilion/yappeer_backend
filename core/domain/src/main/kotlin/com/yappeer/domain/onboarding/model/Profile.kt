package com.yappeer.domain.onboarding.model

import java.util.UUID

data class Profile(
    val id: UUID,
    val username: String,
    val email: String,
    val avatar: String?,
)
