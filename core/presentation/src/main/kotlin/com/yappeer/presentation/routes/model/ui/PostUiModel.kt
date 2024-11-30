package com.yappeer.presentation.routes.model.ui

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PostUiModel(
    val id: String,
    val title: String?,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val createdBy: String,
    val communityIds: List<CommunityUiModel>,
    val shares: Int = 0,
    val tags: List<TagUiModel>,
)
