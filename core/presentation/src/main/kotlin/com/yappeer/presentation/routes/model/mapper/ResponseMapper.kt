package com.yappeer.presentation.routes.model.mapper

import com.yappeer.domain.onboarding.model.Profile
import com.yappeer.presentation.routes.model.ProfileUiModel

object ResponseMapper {

    fun Profile.toUiModel(): ProfileUiModel {
        return ProfileUiModel(
            id = id.toString(),
            username = username,
            email = email,
            avatar = avatar,
        )
    }
}
