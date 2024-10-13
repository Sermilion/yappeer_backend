package com.yappeer.presentation.routes.model.param

import com.yappeer.domain.onboarding.model.value.Email
import com.yappeer.domain.onboarding.model.value.Password
import kotlinx.serialization.Serializable

@Serializable
data class LoginParams(
    private val email: String,
    private val password: String,
) {
    val emailValue get() = Email(email)
    val passwordValue get() = Password(password)
}
