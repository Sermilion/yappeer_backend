package com.sermilion.domain.onboarding.repository.model

sealed interface RegistrationResult {
    data class Error(val errorType: RegistrationErrorType) : RegistrationResult
    data object Success : RegistrationResult

    sealed interface RegistrationErrorType {
        data object PasswordMatch : RegistrationErrorType
        data object UsernameOrEmailTaken : RegistrationErrorType
        data class Validation(val validationErrors: List<ValidationType>) : RegistrationErrorType
        data object UnknownError : RegistrationErrorType

        enum class ValidationType {
            Password,
            Email,
            Username,
        }
    }
}
