package com.yappeer.data.communities

import com.yappeer.data.communities.datasource.db.dao.CommunitiesDAO
import com.yappeer.data.communities.datasource.db.dao.PostDAO
import com.yappeer.domain.communities.model.Community
import com.yappeer.domain.posts.model.Post
import com.yappeer.domain.subscriptions.model.Tag
import kotlinx.datetime.toKotlinInstant

object CommunitiesMapper {

    fun PostDAO.toDomainModel(
        communities: List<Community>,
        tags: List<Tag>,
    ): Post {
        return Post(
            id = this.id.value,
            title = this.title,
            content = this.content,
            createdBy = this.createdBy.value,
            createdAt = this.createdAt.toKotlinInstant(),
            updatedAt = this.updatedAt?.toKotlinInstant(),
            likes = this.likes,
            dislikes = this.dislikes,
            shares = this.shares,
            communities = communities,
            tags = tags,
        )
    }

    fun CommunitiesDAO.toDomainModel(): Community {
        return Community(
            id = this.id.value,
            name = this.name,
            description = this.description,
            creatorId = this.creatorId.value,
            createdAt = this.createdAt.toKotlinInstant(),
            updatedAt = this.updatedAt?.toKotlinInstant(),
            isPrivate = this.isPrivate,
            rules = null,
            communityImageUrl = null,
        )
    }
}
