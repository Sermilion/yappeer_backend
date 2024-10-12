package com.yappeer.domain.onboarding.model.result

sealed interface RegistrationResult {
    data class Error(val errorType: RegistrationErrorType) : RegistrationResult
    data class Success(val user: Data) : RegistrationResult

    sealed interface RegistrationErrorType {
        data object UsernameOrEmailTaken : RegistrationErrorType
        data object UnknownError : RegistrationErrorType
    }

    data class Data(
        val id: String,
        val username: String,
        val email: String,
    )
}
