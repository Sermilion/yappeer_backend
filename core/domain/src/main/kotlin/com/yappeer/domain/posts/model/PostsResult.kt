package com.yappeer.domain.posts.model

data class PostsResult(
    val posts: List<Post>,
    val totalCount: Long,
    val pagesCount: Long,
    val currentPage: Int,
)
