package com.yappeer.domain.content.datasource

import com.yappeer.domain.content.model.Tag
import com.yappeer.domain.onboarding.model.User
import java.util.UUID

interface SubscriptionsDataSource {
    fun findFollowers(userId: UUID, page: Int, pageSize: Int): List<User>
    fun findFollowing(userId: UUID, page: Int, pageSize: Int): List<User>
    fun findFollowedTags(userId: UUID, page: Int, pageSize: Int): List<Tag>
}
