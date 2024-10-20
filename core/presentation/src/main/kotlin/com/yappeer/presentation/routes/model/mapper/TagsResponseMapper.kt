package com.yappeer.presentation.routes.model.mapper

import com.yappeer.domain.content.model.Tag
import com.yappeer.domain.content.model.TagsResult
import com.yappeer.presentation.routes.model.ui.TagUiModel
import com.yappeer.presentation.routes.model.ui.TagsUiModel

internal object TagsResponseMapper {

    fun TagsResult.Data.toUiModel(): TagsUiModel {
        return TagsUiModel(
            tags = this.tags.map { it.toUiModel() },
            totalTagCount = this.totalTagCount,
            pagesCount = this.pagesCount,
            currentPage = this.currentPage,
        )
    }

    private fun Tag.toUiModel(): TagUiModel {
        return TagUiModel(
            id = id.toString(),
            name = name,
        )
    }
}
