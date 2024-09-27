package com.sermilion.domain.onboarding.repository

interface OnboardingRepository {

    suspend fun register(
        username: String,
        password: String,
        repeatPassword: String,
        email: String,
    ): RegistrationResult

    sealed interface RegistrationResult {
        data class Error(val type: RegistrationType): RegistrationResult
        data object Success: RegistrationResult
    }

    enum class RegistrationType {
        PasswordMatch,
        UsernameOrEmailTaken,
        DatabaseIssue,
        UnknownError,
    }

}