package com.yappeer.data.posts.repository

import com.yappeer.domain.posts.datasource.PostDataSource
import com.yappeer.domain.posts.model.LikeStatus
import com.yappeer.domain.posts.model.Post
import com.yappeer.domain.posts.model.PostsResult
import com.yappeer.domain.posts.repository.PostsRepository
import java.util.UUID

class YappeerPostsRepository(
    private val dataSource: PostDataSource,
) : PostsRepository {

    override fun userPosts(userId: UUID, page: Int, pageSize: Int): PostsResult? {
        return dataSource.userPosts(userId, page, pageSize)
    }

    override fun createPost(
        title: String,
        content: String,
        tags: List<String>,
        communityIds: List<UUID>,
        mediaUrls: List<String>,
        createdBy: UUID,
    ): Post? {
        return dataSource.createPost(
            title = title,
            content = content,
            tags = tags,
            communityIds = communityIds,
            mediaUrls = mediaUrls,
            createdBy = createdBy,
        )
    }

    override fun homePosts(page: Int, pageSize: Int): PostsResult? {
        return dataSource.homePosts(page, pageSize)
    }

    override fun likePost(postId: UUID, userId: UUID, status: LikeStatus): Boolean {
        return dataSource.updateLikeStats(postId = postId, userId = userId, status = status)
    }
}
