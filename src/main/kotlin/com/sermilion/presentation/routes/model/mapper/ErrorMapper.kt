package com.sermilion.presentation.routes.model.mapper

import com.sermilion.domain.onboarding.model.result.ErrorDetail
import com.sermilion.domain.onboarding.model.value.Email
import com.sermilion.domain.onboarding.model.value.Password
import com.sermilion.domain.onboarding.model.value.Token
import com.sermilion.domain.onboarding.model.value.Username

fun List<String>.toPresentationModel(): List<ErrorDetail> {
    return this.map {
        when (it) {
            Password::class.simpleName -> ErrorDetail(
                field = "password",
                detail = "Password is invalid. It should be at least 8 characters and contain at least 1 special " +
                    "character.",
            )

            Email::class.simpleName -> ErrorDetail(
                field = "email",
                detail = "Email is invalid.",
            )

            Username::class.simpleName -> ErrorDetail(
                field = "username",
                detail = "Username is too short.",
            )
            Token::class.simpleName -> ErrorDetail(
                field = "token",
                detail = "Token is invalid.",
            )
            else -> ErrorDetail(
                field = "unknown",
                detail = "Unknown error.",
            )
        }
    }
}
