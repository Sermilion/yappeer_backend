package com.yappeer.domain.subscriptions.repository

import com.yappeer.domain.subscriptions.model.CommunitiesResult
import com.yappeer.domain.subscriptions.model.FollowersResult
import com.yappeer.domain.subscriptions.model.TagsResult
import java.util.UUID

interface SubscriptionsRepository {
    fun findFollowers(userId: UUID, page: Int, pageSize: Int): FollowersResult
    fun findFollowing(userId: UUID, page: Int, pageSize: Int): FollowersResult
    fun findFollowedTags(userId: UUID, page: Int, pageSize: Int): TagsResult
    fun findFollowedCommunities(userId: UUID, page: Int, pageSize: Int): CommunitiesResult?
}
