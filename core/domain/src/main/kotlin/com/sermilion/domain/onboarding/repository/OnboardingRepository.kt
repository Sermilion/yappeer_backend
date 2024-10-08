package com.sermilion.domain.onboarding.repository

import com.sermilion.domain.onboarding.model.Profile
import com.sermilion.domain.onboarding.model.result.RegistrationResult
import com.sermilion.domain.onboarding.model.value.Email
import com.sermilion.domain.onboarding.model.value.Password
import com.sermilion.domain.onboarding.model.value.Username

interface OnboardingRepository {

    fun register(
        username: Username,
        hashedPassword: Password,
        email: Email,
    ): RegistrationResult

    fun findPassword(username: Username): Password?

    fun findUser(username: Username): Profile?
}
