package com.yappeer.presentation.routes.model.mapper

import com.yappeer.domain.subscriptions.model.FollowersResult
import com.yappeer.presentation.routes.model.mapper.UserResponseMapper.toUiModel
import com.yappeer.presentation.routes.model.ui.FollowersUiModel
import com.yappeer.presentation.routes.model.ui.PaginationUiModel

internal object FollowersResponseMapper {

    fun FollowersResult.Data.toUiModel(): FollowersUiModel {
        return FollowersUiModel(
            users = this.users.map { it.toUiModel() },
            pagination = PaginationUiModel(
                totalCount = this.totalUserCount,
                pagesCount = this.pagesCount,
                currentPage = this.currentPage,
            ),
        )
    }
}
