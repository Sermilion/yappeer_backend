package com.yappeer.presentation.routes.model.ui

import kotlinx.serialization.Serializable

@Serializable
data class CommunitiesUiModel(
    val communities: List<CommunityUiModel>,
    val pagination: PaginationUiModel,
)
