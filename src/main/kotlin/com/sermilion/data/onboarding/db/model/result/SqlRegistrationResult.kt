package com.sermilion.data.onboarding.db.model.result

import com.sermilion.data.onboarding.model.registration.UserResultDataModel

sealed interface SqlRegistrationResult {
    data class Success(val user: UserResultDataModel) : SqlRegistrationResult
    data object ConstraintViolation : SqlRegistrationResult
    data object UnknownError : SqlRegistrationResult
}
