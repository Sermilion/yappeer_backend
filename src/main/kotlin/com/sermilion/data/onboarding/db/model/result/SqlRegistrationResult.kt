package com.sermilion.data.onboarding.db.model.result

import com.sermilion.data.onboarding.model.UserDataModel

sealed interface SqlRegistrationResult {
    data class Success(val user: UserDataModel) : SqlRegistrationResult
    data object ConstraintViolation : SqlRegistrationResult
    data object UnknownError : SqlRegistrationResult
}
