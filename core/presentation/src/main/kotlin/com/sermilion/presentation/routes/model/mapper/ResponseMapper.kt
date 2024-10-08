package com.sermilion.presentation.routes.model.mapper

import com.sermilion.domain.onboarding.model.Profile
import com.sermilion.presentation.routes.model.ProfileUiModel

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
