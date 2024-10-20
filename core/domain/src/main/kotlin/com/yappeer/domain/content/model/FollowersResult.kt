package com.yappeer.domain.content.model

import com.yappeer.domain.onboarding.model.User

sealed interface FollowersResult {
    data class Data(
        val users: List<User>,
        val pagesCount: Long,
        val currentPage: Int,
    ) : FollowersResult

    data object Error : FollowersResult
}
