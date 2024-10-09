package com.yappeer.presentation.routes.model.param

import com.yappeer.domain.onboarding.model.value.Password
import com.yappeer.domain.onboarding.model.value.Username
import kotlinx.serialization.Serializable

@Serializable
data class LoginParams(
    private val username: String,
    private val password: String,
) {
    val usernameValue get() = Username(username)
    val passwordValue get() = Password(password)
}
