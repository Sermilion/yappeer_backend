package com.yappeer.presentation.routes.model.mapper

import com.yappeer.domain.onboarding.model.User
import com.yappeer.presentation.routes.model.ui.UserUiModel

internal object UserResponseMapper {

    fun User.toUiModel(): UserUiModel {
        return UserUiModel(
            id = id.toString(),
            username = username,
            email = email,
            avatar = avatar,
            bio = bio,
            createdAt = createdAt,
            lastLogin = lastLogin,
            background = background,
        )
    }
}
