package com.sermilion.data.onboarding.db.model.result

import com.sermilion.data.onboarding.model1.registration.UserResultDataModel

sealed interface SqlResult {
    data class Success(val user: UserResultDataModel) : SqlResult
    data object ConstraintViolation : SqlResult
    data object UnknownError : SqlResult
}
