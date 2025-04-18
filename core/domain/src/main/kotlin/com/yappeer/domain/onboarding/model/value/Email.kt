package com.yappeer.domain.onboarding.model.value

@JvmInline
value class Email(val value: String) : Value {

    override val errorMessage: String get() = "Email is invalid."

    init {
        val emailIsValid = value.isNotEmpty() && value.matches(Regex(pattern = EMAIL_PATTERN))
        if (!emailIsValid) {
            throw ValueValidationException(this)
        }
    }

    companion object {
        private const val EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    }
}
