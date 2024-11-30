package com.yappeer.domain.subscriptions.model

import com.yappeer.domain.onboarding.model.User

sealed interface FollowersResult {
    data class Data(
        val users: List<User>,
        val totalUserCount: Long,
        val pagesCount: Long,
        val currentPage: Int,
    ) : FollowersResult

    data object Error : FollowersResult
}
