package com.yappeer.presentation.routes.model.ui

data class FollowersUiModel(
    val users: List<UserUiModel>,
    val pagesCount: Long,
    val currentPage: Int,
)
