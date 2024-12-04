package com.yappeer.presentation.routes.model.param

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedRequestParams(
    val page: Int,
    val pageSize: Int,
)
