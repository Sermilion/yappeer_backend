package com.yappeer.domain.content.repository

import com.yappeer.domain.content.model.FollowersResult
import com.yappeer.domain.content.model.TagsResult
import java.util.UUID

interface SubscriptionsRepository {
    fun findFollowers(userId: UUID, page: Int, pageSize: Int): FollowersResult
    fun findFollowing(userId: UUID, page: Int, pageSize: Int): FollowersResult
    fun findFollowedTags(userId: UUID, page: Int, pageSize: Int): TagsResult
}
