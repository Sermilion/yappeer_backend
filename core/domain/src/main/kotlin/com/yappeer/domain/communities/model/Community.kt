package com.yappeer.domain.communities.model

import kotlinx.datetime.Instant
import java.util.UUID

data class Community(
    val id: UUID,
    val name: String,
    val description: String?,
    val creatorId: UUID,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val isPrivate: Boolean,
    val rules: List<String>?,
    val iconUrl: String?,
    val followersCount: Long,
)
