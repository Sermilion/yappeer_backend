package com.yappeer.presentation.routes.model.ui

import kotlinx.serialization.Serializable

@Serializable
data class TokenUiModel(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long = 3600,
    val tokenType: String = "Bearer",
)
