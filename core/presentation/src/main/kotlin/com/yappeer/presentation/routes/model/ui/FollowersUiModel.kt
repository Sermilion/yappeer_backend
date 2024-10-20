package com.yappeer.presentation.routes.model.ui

import kotlinx.serialization.Serializable

@Serializable
data class FollowersUiModel(
    val users: List<UserUiModel>,
    val totalUserCount: Long,
    val pagesCount: Long,
    val currentPage: Int,
)
