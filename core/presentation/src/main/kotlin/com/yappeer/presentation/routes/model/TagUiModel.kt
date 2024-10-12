package com.yappeer.presentation.routes.model

import kotlinx.serialization.Serializable

@Serializable
data class TagUiModel(
    val id: String,
    val name: String,
)
