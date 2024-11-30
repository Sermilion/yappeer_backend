package com.yappeer.presentation.routes.model.ui

import kotlinx.serialization.Serializable

@Serializable
data class UserPostsUiModel(
    val posts: List<PostUiModel>,
    val pagination: PaginationUiModel,
)
