package com.sermilion.presentation.routes.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenUiModel(
    val accessToken: String,
    val refreshToken: String,
)
