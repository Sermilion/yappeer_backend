package com.yappeer.presentation.routes.model.ui

import kotlinx.serialization.Serializable

@Serializable
data class TagUiModel(
    val id: String,
    val name: String,
)
