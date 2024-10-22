package com.yappeer.presentation.routes.model.ui

import kotlinx.serialization.Serializable

@Serializable
data class PaginationUiModel(
    val totalCount: Long,
    val pagesCount: Long,
    val currentPage: Int,
)
