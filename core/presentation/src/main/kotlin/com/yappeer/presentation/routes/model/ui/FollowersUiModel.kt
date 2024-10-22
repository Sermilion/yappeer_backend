package com.yappeer.presentation.routes.model.ui

import kotlinx.serialization.Serializable

@Serializable
data class FollowersUiModel(
    val users: List<UserUiModel>,
    val pagination: PaginationUiModel,
)
