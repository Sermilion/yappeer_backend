package com.sermilion.domain.onboarding.model.result

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse<T>(
    val code: String,
    val details: T,
)

@Serializable
data class ErrorDetail(
    val field: String,
    val detail: String,
)
