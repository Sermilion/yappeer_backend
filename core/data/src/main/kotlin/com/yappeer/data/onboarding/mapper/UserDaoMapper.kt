package com.yappeer.data.onboarding.mapper

import com.yappeer.data.onboarding.datasource.db.dao.UserDAO
import com.yappeer.domain.onboarding.model.User
import kotlinx.datetime.toKotlinInstant

internal object UserDaoMapper {
    fun UserDAO.toDomainModel(): User {
        return User(
            id = this.id.value,
            username = this.username,
            email = this.email,
            createdAt = this.createdAt.toKotlinInstant(),
            lastLogin = this.lastLogin?.toKotlinInstant(),
            bio = this.bio,
            avatar = this.avatar,
            background = this.background,
        )
    }
}
