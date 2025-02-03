package com.yappeer.domain.posts.datasource

import com.yappeer.domain.posts.model.LikeStatus
import com.yappeer.domain.posts.model.PostsResult
import java.util.UUID

interface PostDataSource {
    fun userPosts(userId: UUID, page: Int, pageSize: Int): PostsResult?
    fun createPost(
        title: String,
        content: String,
        tags: List<String>,
        createdBy: UUID,
    ): Boolean
    fun homePosts(page: Int, pageSize: Int): PostsResult?
    fun updateLikeStats(postId: UUID, userId: UUID, status: LikeStatus) : Boolean
}
