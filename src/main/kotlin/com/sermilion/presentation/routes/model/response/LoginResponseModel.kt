package com.sermilion.presentation.routes.model.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseModel(
    val accessToken: String,
    val refreshToken: String,
)
