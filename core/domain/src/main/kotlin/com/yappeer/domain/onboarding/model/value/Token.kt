package com.yappeer.domain.onboarding.model.value

data class Token(val value: String) : Value {

    override val errorMessage: String get() = "Token is invalid."

    init {
        if (value.isEmpty()) {
            throw ValueValidationException(this)
        }
    }
}
