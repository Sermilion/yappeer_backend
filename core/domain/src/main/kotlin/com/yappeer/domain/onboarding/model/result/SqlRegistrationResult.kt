package com.yappeer.domain.onboarding.model.result

import com.yappeer.domain.onboarding.model.User

sealed interface SqlRegistrationResult {
    data class Success(val user: User) : SqlRegistrationResult
    data object ConstraintViolation : SqlRegistrationResult
    data object UnknownError : SqlRegistrationResult
}
