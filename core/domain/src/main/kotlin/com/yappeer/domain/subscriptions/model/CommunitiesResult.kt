package com.yappeer.domain.subscriptions.model

import com.yappeer.domain.communities.model.Community

data class CommunitiesResult(
    val communities: List<Community>,
    val pagesCount: Long,
    val currentPage: Int,
    val totalTagCount: Long,
)
