package com.sermilion.domain.onboarding.repository

import com.sermilion.domain.onboarding.model.registration.Email
import com.sermilion.domain.onboarding.model.registration.Password
import com.sermilion.domain.onboarding.model.registration.Username
import com.sermilion.domain.onboarding.repository.model.RegistrationResult

interface OnboardingRepository {

    suspend fun register(
        username: Username,
        password: Password,
        repeatPassword: Password,
        email: Email,
    ): RegistrationResult
}
