package com.sermilion.domain.onboarding.repository

import com.sermilion.domain.onboarding.model.registration.value.Email
import com.sermilion.domain.onboarding.model.registration.value.Password
import com.sermilion.domain.onboarding.model.registration.value.Username
import com.sermilion.domain.onboarding.model.registration.result.RegistrationResult

interface OnboardingRepository {

    suspend fun register(
        username: Username,
        password: Password,
        repeatPassword: Password,
        email: Email,
    ): RegistrationResult
}
