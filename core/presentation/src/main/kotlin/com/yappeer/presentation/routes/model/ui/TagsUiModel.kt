package com.yappeer.presentation.routes.model.ui

import kotlinx.serialization.Serializable

@Serializable
data class TagsUiModel(
    val tags: List<TagUiModel>,
    val totalTagCount: Long,
    val pagesCount: Long,
    val currentPage: Int,
)
