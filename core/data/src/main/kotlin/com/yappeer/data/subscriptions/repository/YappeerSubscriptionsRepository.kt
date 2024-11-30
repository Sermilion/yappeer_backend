package com.yappeer.data.subscriptions.repository

import com.yappeer.domain.subscriptions.datasource.SubscriptionsDataSource
import com.yappeer.domain.subscriptions.model.FollowersResult
import com.yappeer.domain.subscriptions.model.TagsResult
import com.yappeer.domain.subscriptions.repository.SubscriptionsRepository
import java.util.UUID

class YappeerSubscriptionsRepository(
    private val dataSource: SubscriptionsDataSource,
) : SubscriptionsRepository {
    override fun findFollowers(userId: UUID, page: Int, pageSize: Int): FollowersResult {
        return dataSource.findFollowers(userId, page, pageSize)
    }

    override fun findFollowing(userId: UUID, page: Int, pageSize: Int): FollowersResult {
        return dataSource.findFollowing(userId, page, pageSize)
    }

    override fun findFollowedTags(userId: UUID, page: Int, pageSize: Int): TagsResult {
        return dataSource.findFollowedTags(userId, page, pageSize)
    }
}
