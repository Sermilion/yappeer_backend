package com.yappeer.presentation.routes.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileUiModel(
    val id: String,
    val username: String,
    val email: String,
    val avatar: String?,
)
