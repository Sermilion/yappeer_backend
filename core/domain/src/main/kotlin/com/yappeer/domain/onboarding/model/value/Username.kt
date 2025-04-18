package com.yappeer.domain.onboarding.model.value

@JvmInline
value class Username(val value: String) : Value {

    override val errorMessage: String get() = "Username is too short."

    init {
        if (value.isEmpty() || value.length < MIN_USERNAME_LENGTH) {
            throw ValueValidationException(this)
        }
    }

    private companion object {
        const val MIN_USERNAME_LENGTH = 2
    }
}
