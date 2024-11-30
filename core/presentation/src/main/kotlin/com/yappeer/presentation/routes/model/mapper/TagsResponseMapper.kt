package com.yappeer.presentation.routes.model.mapper

import com.yappeer.domain.subscriptions.model.Tag
import com.yappeer.domain.subscriptions.model.TagsResult
import com.yappeer.presentation.routes.model.ui.PaginationUiModel
import com.yappeer.presentation.routes.model.ui.TagUiModel
import com.yappeer.presentation.routes.model.ui.TagsUiModel

internal object TagsResponseMapper {

    fun TagsResult.Data.toUiModel(): TagsUiModel {
        return TagsUiModel(
            tags = this.tags.map { it.toUiModel() },
            pagination = PaginationUiModel(
                totalCount = this.totalTagCount,
                pagesCount = this.pagesCount,
                currentPage = this.currentPage,
            ),
        )
    }

    private fun Tag.toUiModel(): TagUiModel {
        return TagUiModel(
            id = id.toString(),
            name = name,
            followers = followers,
        )
    }
}
