package com.sermilion.presentation.routes.model.mapper

import com.sermilion.domain.onboarding.model.value.ValueType
import com.sermilion.presentation.routes.model.response.ErrorDetail

fun List<ValueType>.toPresentationModel(): List<ErrorDetail> {
    return this.map {
        when (it) {
            ValueType.Password -> ErrorDetail(
                field = "password",
                detail = "Password is invalid. It should be at least 8 characters and contain at least 1 special " +
                    "character.",
            )

            ValueType.Email -> ErrorDetail(
                field = "email",
                detail = "Email is invalid.",
            )

            ValueType.Username -> ErrorDetail(
                field = "username",
                detail = "Username is too short.",
            )
        }
    }
}
