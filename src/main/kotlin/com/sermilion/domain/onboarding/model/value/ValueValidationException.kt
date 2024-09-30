package com.sermilion.domain.onboarding.model.value

class ValueValidationException(
    val valueType: ValueType,
) : Exception("Validation for value type $valueType failed.")
