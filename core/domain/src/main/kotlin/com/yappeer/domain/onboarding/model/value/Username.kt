package com.yappeer.domain.onboarding.model.value

@JvmInline
value class Username(val value: String) {
    init {
        if (value.isEmpty() || value.length < MIN_USERNAME_LENGTH) {
            throw ValueValidationException(Username::class.simpleName.orEmpty())
        }
    }

    private companion object {
        const val MIN_USERNAME_LENGTH = 2
    }
}
