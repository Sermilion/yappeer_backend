package com.yappeer.data.onboarding.mapper

import com.yappeer.data.subscriptions.datasource.db.dao.TagDAO
import com.yappeer.domain.subscriptions.model.Tag

object TagDaoMapper {
    fun TagDAO.toDomainModel(followers: Long): Tag {
        return Tag(
            id = this.id.value,
            name = this.name,
            followers = followers,
        )
    }
}
