package com.sermilion.domain.onboarding.model.registration.result

import com.sermilion.presentation.routes.model.response.UserResponse

sealed interface RegistrationResult {
    data class Error(val errorType: RegistrationErrorType) : RegistrationResult
    data class Success(val user: UserResponse) : RegistrationResult

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
