package com.yappeer.data.onboarding.mapper

import com.yappeer.data.content.datasource.db.dao.TagDAO
import com.yappeer.domain.content.model.Tag

object TagDaoMapper {
    fun TagDAO.toDomainModel(followers: Long): Tag {
        return Tag(
            id = this.id.value,
            name = this.name,
            followers = followers,
        )
    }
}
