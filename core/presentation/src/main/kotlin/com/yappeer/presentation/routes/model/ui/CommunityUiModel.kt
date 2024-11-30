package com.yappeer.presentation.routes.model.ui

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CommunityUiModel(
    val id: String,
    val name: String,
    val description: String?,
    val creatorId: String,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val isPrivate: Boolean,
    val rules: List<String>?,
    val communityImageUrl: String?,
)
