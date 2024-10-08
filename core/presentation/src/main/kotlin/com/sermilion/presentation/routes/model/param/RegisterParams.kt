package com.sermilion.presentation.routes.model.param

import com.sermilion.domain.onboarding.model.value.Email
import com.sermilion.domain.onboarding.model.value.Password
import com.sermilion.domain.onboarding.model.value.Username
import kotlinx.serialization.Serializable

@Serializable
class RegisterParams(
    val username: String,
    val password: String,
    val repeatPassword: String,
    val email: String,
) {
    val usernameValue get() = Username(username)
    val passwordValue get() = Password(password)
    val repeatPasswordValue get() = Password(repeatPassword)
    val emailValue get() = Email(email)
}
