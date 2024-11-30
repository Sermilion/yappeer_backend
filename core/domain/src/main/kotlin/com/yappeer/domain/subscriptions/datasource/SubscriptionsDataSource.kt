package com.yappeer.domain.subscriptions.datasource

import com.yappeer.domain.subscriptions.model.FollowersResult
import com.yappeer.domain.subscriptions.model.TagsResult
import java.util.UUID

interface SubscriptionsDataSource {
    fun findFollowers(userId: UUID, page: Int, pageSize: Int): FollowersResult
    fun findFollowing(userId: UUID, page: Int, pageSize: Int): FollowersResult
    fun findFollowedTags(userId: UUID, page: Int, pageSize: Int): TagsResult
}
