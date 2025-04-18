package com.yappeer.presentation.routes.model.result

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val code: String,
    val details: List<ErrorDetail> = emptyList(),
    val message: String? = null,
)

@Serializable
data class ErrorDetail(
    val field: String,
    val detail: String,
)
