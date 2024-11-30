package com.yappeer.data.posts.repository

import com.yappeer.domain.posts.datasource.PostDataSource
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
        createdBy: UUID,
    ): Boolean {
        return dataSource.createPost(
            title = title,
            content = content,
            tags = tags,
            createdBy = createdBy,
        )
    }
}
