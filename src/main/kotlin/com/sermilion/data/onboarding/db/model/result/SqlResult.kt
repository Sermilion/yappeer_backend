package com.sermilion.data.onboarding.db.model.result

sealed interface SqlResult {
    data object Success : SqlResult
    data object ConstraintViolation : SqlResult
    data object UnknownError : SqlResult
}
