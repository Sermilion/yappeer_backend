package com.yappeer.presentation.routes.model.mapper

import com.yappeer.domain.communities.model.Community
import com.yappeer.domain.posts.model.Post
import com.yappeer.domain.posts.model.PostsResult
import com.yappeer.domain.subscriptions.model.Tag
import com.yappeer.presentation.routes.model.ui.CommunityUiModel
import com.yappeer.presentation.routes.model.ui.PaginationUiModel
import com.yappeer.presentation.routes.model.ui.PostUiModel
import com.yappeer.presentation.routes.model.ui.TagUiModel
import com.yappeer.presentation.routes.model.ui.UserPostsUiModel

object PostMapper {
    fun PostsResult.toUiModel(): UserPostsUiModel {
        return UserPostsUiModel(
            posts = posts.map { it.toUiModel() },
            pagination = PaginationUiModel(
                totalCount = totalCount,
                pagesCount = pagesCount,
                currentPage = currentPage,
            ),
        )
    }

    fun Post.toUiModel(): PostUiModel {
        return PostUiModel(
            id = id.toString(),
            title = title,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            likes = likes,
            dislikes = dislikes,
            createdBy = createdBy.toString(),
            communityIds = communities.map { it.toUiModel() },
            shares = shares,
            tags = tags.map { it.toUiModel() },
        )
    }

    fun Community.toUiModel(): CommunityUiModel {
        return CommunityUiModel(
            id = id.toString(),
            name = name,
            description = description,
            creatorId = creatorId.toString(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            isPrivate = isPrivate,
            rules = rules,
            communityImageUrl = communityImageUrl,
        )
    }

    fun Tag.toUiModel(): TagUiModel {
        return TagUiModel(
            id = id.toString(),
            name = name,
            followers = followers,
        )
    }
}
