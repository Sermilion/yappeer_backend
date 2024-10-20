package com.yappeer.domain.content.model

sealed interface TagResult {
    data class Data(
        val tags: List<Tag>,
        val pagesCount: Long,
        val currentPage: Int,
    ) : TagResult

    data object Error : TagResult
}
