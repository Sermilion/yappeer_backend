package com.yappeer.data.content.repository

import com.yappeer.domain.content.datasource.SubscriptionsDataSource
import com.yappeer.domain.content.model.FollowersResult
import com.yappeer.domain.content.model.TagResult
import com.yappeer.domain.content.repository.SubscriptionsRepository
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

    override fun findFollowedTags(userId: UUID, page: Int, pageSize: Int): TagResult {
        return dataSource.findFollowedTags(userId, page, pageSize)
    }
}
