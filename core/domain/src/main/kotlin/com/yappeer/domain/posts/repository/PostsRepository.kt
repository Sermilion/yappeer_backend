package com.yappeer.domain.posts.repository

import com.yappeer.domain.posts.model.LikeStatus
import com.yappeer.domain.posts.model.Post
import com.yappeer.domain.posts.model.PostsResult
import java.util.UUID

interface PostsRepository {
    fun userPosts(userId: UUID, page: Int, pageSize: Int): PostsResult?
    fun createPost(
        title: String,
        content: String,
        tags: List<String>,
        createdBy: UUID,
    ): Post?
    fun homePosts(page: Int, pageSize: Int): PostsResult?
    fun likePost(postId: UUID, userId: UUID, status: LikeStatus): Boolean
}
