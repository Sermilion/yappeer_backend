package com.yappeer.domain.posts.model

import com.yappeer.domain.communities.model.Community
import com.yappeer.domain.subscriptions.model.Tag
import kotlinx.datetime.Instant
import java.util.UUID

data class Post(
    val id: UUID,
    val title: String?,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val createdBy: UUID,
    val communities: List<Community>,
    val shares: Int = 0,
    val tags: List<Tag>,
)
