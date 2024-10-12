package com.yappeer.presentation.routes.model.mapper

import com.yappeer.domain.onboarding.model.Profile
import com.yappeer.presentation.routes.model.ProfileUiModel
import com.yappeer.presentation.routes.model.TagUiModel

object ResponseMapper {

    fun Profile.toUserProfileUiModel(): ProfileUiModel {
        return ProfileUiModel(
            id = id.toString(),
            username = username,
            email = email,
            avatar = avatar,
            bio = bio,
            tags = tags.map {
                TagUiModel(
                    id = it.id.toString(),
                    name = it.name,
                )
            },
        )
    }
}
