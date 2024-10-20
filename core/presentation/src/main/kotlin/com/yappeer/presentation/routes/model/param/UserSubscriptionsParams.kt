package com.yappeer.presentation.routes.model.param

import kotlinx.serialization.Serializable

@Serializable
data class UserSubscriptionsParams(
    val userId: String,
    val page: Int,
    val pageSize: Int,
)
