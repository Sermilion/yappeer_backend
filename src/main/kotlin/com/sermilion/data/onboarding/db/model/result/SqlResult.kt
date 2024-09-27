package com.sermilion.data.onboarding.db.model.result

sealed interface SQLResult {
    data object Success: SQLResult
    data object ConstraintViolation: SQLResult
    data object UnknownError: SQLResult
}
