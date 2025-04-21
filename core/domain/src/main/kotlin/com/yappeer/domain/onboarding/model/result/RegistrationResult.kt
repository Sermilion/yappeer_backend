package com.yappeer.domain.onboarding.model.result

import com.yappeer.domain.onboarding.model.User

sealed interface RegistrationResult {
    data class Error(val errorType: RegistrationErrorType) : RegistrationResult
    data class Success(val user: User) : RegistrationResult

    sealed interface RegistrationErrorType {
        data object UsernameTaken : RegistrationErrorType
        data object EmailTaken : RegistrationErrorType
    }
}
