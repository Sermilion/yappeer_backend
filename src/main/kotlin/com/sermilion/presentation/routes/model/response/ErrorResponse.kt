package com.sermilion.presentation.routes.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val code: String,
    val details: List<ErrorDetail>,
)

@Serializable
data class ErrorDetail(
    val field: String,
    val detail: String,
)
