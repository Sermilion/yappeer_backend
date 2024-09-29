package com.sermilion.domain.onboarding.model.registration.value

@JvmInline
value class Email(val value: String) : Validateable {
    override fun validate(): Boolean {
        return value.isNotEmpty() && value.matches(Regex(pattern = EMAIL_PATTERN))
    }

    companion object {
        private const val EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    }
}
