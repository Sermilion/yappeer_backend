package com.yappeer.domain.onboarding.model

import kotlinx.datetime.Instant
import java.util.UUID

data class Profile(
    val id: UUID,
    val username: String,
    val email: String,
    val avatar: String?,
    val bio: String?,
    val createdAt: Instant,
    val lastLogin: Instant?,
    val tags: List<Tag>,
)
