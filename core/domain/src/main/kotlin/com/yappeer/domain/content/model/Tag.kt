package com.yappeer.domain.content.model

import java.util.UUID

data class Tag(
    val id: UUID,
    val name: String,
    val followers: Long,
)
