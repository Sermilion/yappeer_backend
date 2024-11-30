package com.yappeer.data.communities.repository

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
}
