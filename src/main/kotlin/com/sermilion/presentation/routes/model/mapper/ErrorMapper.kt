package com.sermilion.presentation.routes.model.mapper

import com.sermilion.domain.onboarding.repository.model.RegistrationResult.RegistrationErrorType.ValidationType
import com.sermilion.presentation.routes.model.ErrorDetail

fun List<ValidationType>.toPresentationModel(): List<ErrorDetail> {
    return this.map {
        when (it) {
            ValidationType.Password -> ErrorDetail(
                field = "password",
                detail = "Password is invalid. It should be at least 8 characters and contain at least 1 special " +
                    "character.",
            )

            ValidationType.Email -> ErrorDetail(
                field = "email",
                detail = "Email is invalid.",
            )

            ValidationType.Username -> ErrorDetail(
                field = "username",
                detail = "Username is too short.",
            )
        }
    }
}
