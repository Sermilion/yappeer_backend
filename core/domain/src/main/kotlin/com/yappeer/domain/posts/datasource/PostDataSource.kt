package com.yappeer.domain.posts.datasource

import com.yappeer.domain.posts.model.PostsResult
import java.util.UUID

interface PostDataSource {
    fun userPosts(userId: UUID, page: Int, pageSize: Int): PostsResult?
}