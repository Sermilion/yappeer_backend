package com.yappeer.presentation.routes.model.ui

import kotlinx.serialization.Serializable

@Serializable
data class PaginationUiModel(
    val totalUserCount: Long,
    val pagesCount: Long,
    val currentPage: Int,
)
