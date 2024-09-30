package com.sermilion.presentation.routes.model.response

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationResponseModel(
    val id: String,
    val username: String,
    val email: String,
    val avatar: String?,
)
