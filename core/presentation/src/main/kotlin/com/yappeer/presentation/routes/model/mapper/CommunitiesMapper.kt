package com.yappeer.presentation.routes.model.mapper

import com.yappeer.domain.communities.model.Community
import com.yappeer.domain.subscriptions.model.CommunitiesResult
import com.yappeer.presentation.routes.model.ui.CommunitiesUiModel
import com.yappeer.presentation.routes.model.ui.CommunityUiModel
import com.yappeer.presentation.routes.model.ui.PaginationUiModel

object CommunitiesMapper {
    fun CommunitiesResult.toUiModel(): CommunitiesUiModel {
        return CommunitiesUiModel(
            communities = this.communities.map { it.toUiModel() },
            pagination = PaginationUiModel(
                totalCount = this.totalTagCount,
                pagesCount = this.pagesCount,
                currentPage = this.currentPage,
            ),
        )
    }

    private fun Community.toUiModel(): CommunityUiModel {
        return CommunityUiModel(
            id = id.toString(),
            name = name,
            description = description,
            creatorId = creatorId.toString(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            isPrivate = isPrivate,
            rules = rules,
            communityImageUrl = iconUrl,
        )
    }
}
