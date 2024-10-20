package com.yappeer.presentation.routes.model.ui

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserUiModel(
    val id: String,
    val username: String,
    val email: String,
    val bio: String?,
    val avatar: String?,
    val createdAt: Instant,
    val lastLogin: Instant?,
    val background: String?,
)
