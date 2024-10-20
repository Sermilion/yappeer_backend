package com.yappeer.presentation.routes.model.ui

import kotlinx.serialization.Serializable

@Serializable
data class SelfProfileUiModel(
    val id: String,
    val username: String,
    val email: String,
    val avatar: String?,
)
