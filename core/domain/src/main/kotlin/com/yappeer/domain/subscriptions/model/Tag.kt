package com.yappeer.domain.subscriptions.model

import java.util.UUID

data class Tag(
    val id: UUID,
    val name: String,
    val followers: Long,
)
