package com.yappeer.presentation.routes.model.ui

import kotlinx.serialization.Serializable

@Serializable
data class TagsUiModel(
    val tags: List<TagUiModel>,
    val pagination: PaginationUiModel,
)
