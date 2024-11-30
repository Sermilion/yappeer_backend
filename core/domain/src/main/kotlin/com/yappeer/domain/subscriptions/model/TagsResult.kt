package com.yappeer.domain.subscriptions.model

sealed interface TagsResult {
    data class Data(
        val tags: List<Tag>,
        val pagesCount: Long,
        val currentPage: Int,
        val totalTagCount: Long,
    ) : TagsResult

    data object Error : TagsResult
}
