package com.yappeer.domain.onboarding.model

import com.yappeer.domain.onboarding.model.value.Password
import java.util.UUID

data class UserWithPassword(
    val id: UUID,
    private val email: String,
    private val username: String,
    private val password: String,
) {
    val passwordValue get() = Password(password)
}
