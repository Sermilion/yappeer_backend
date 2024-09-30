package com.sermilion.presentation.routes.model.param

import com.sermilion.domain.onboarding.model.value.Password
import com.sermilion.domain.onboarding.model.value.Username
import kotlinx.serialization.Serializable

@Serializable
data class LoginParams(
    private val username: String,
    private val password: String,
) {
    val usernameValue get() = Username(username)
    val passwordValue get() = Password(password)
}
