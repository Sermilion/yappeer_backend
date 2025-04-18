package com.yappeer.domain.onboarding.model.value

class ValueValidationException(
    val value: Value,
) : Exception("Validation for value type ${value.name} failed.")
