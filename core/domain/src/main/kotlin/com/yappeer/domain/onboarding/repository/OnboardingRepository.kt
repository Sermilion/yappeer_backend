package com.yappeer.domain.onboarding.repository

import com.yappeer.domain.onboarding.model.Profile
import com.yappeer.domain.onboarding.model.result.RegistrationResult
import com.yappeer.domain.onboarding.model.value.Email
import com.yappeer.domain.onboarding.model.value.Password
import com.yappeer.domain.onboarding.model.value.Username

interface OnboardingRepository {

    fun register(
        username: Username,
        hashedPassword: Password,
        email: Email,
    ): RegistrationResult

    fun findPassword(username: Username): Password?

    fun findUser(username: Username): Profile?
}
