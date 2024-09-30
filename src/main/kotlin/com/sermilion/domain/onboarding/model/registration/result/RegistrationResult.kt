package com.sermilion.domain.onboarding.model.registration.result

import com.sermilion.presentation.routes.model.response.RegistrationResponseModel

sealed interface RegistrationResult {
    data class Error(val errorType: RegistrationErrorType) : RegistrationResult
    data class Success(val user: RegistrationResponseModel) : RegistrationResult

    sealed interface RegistrationErrorType {
        data object UsernameOrEmailTaken : RegistrationErrorType
        data object UnknownError : RegistrationErrorType
    }
}
