package com.yappeer.domain.onboarding.model

import kotlinx.datetime.Instant
import java.util.UUID

data class User(
    val id: UUID,
    val username: String,
    val email: String,
    val bio: String?,
    val avatar: String?,
    val background: String?,
    val createdAt: Instant,
    val lastLogin: Instant?,
    val followers: Long,
    val following: Long,
)
