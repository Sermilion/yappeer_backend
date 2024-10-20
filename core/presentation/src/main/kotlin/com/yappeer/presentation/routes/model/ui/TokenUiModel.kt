package com.yappeer.presentation.routes.model.ui

import kotlinx.serialization.Serializable

@Serializable
data class TokenUiModel(
    val accessToken: String,
    val refreshToken: String,
)
