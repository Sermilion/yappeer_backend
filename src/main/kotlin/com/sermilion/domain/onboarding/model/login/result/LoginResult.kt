package com.sermilion.domain.onboarding.model.login.result

import java.util.UUID

sealed interface LoginResult {
    data class Error(val errorType: LoginErrorType) : LoginResult
    data class Success(val userId: UUID) : LoginResult

    sealed interface LoginErrorType {
        data object CredentialsMatch : LoginErrorType
        data object UnknownError : LoginErrorType
    }
}
