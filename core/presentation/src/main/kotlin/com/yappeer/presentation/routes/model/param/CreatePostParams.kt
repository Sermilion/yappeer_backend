package com.yappeer.presentation.routes.model.param

import kotlinx.serialization.Serializable

@Serializable
data class CreatePostParams(
    val title: String,
    val content: String,
    val tags: List<String>,
)
