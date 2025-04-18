package com.yappeer.domain.onboarding.model.value

import java.util.regex.Pattern

@JvmInline
value class Password(val value: String) : Value {

    override val errorMessage: String
        get() = "Password is invalid. It should be at least 8 characters and contain at least 1 special " +
            "character."

    init {
        val passwordIsValid = value.isNotEmpty() && Pattern.compile(PASSWORD_PATTERN).matcher(value).matches()
        if (!passwordIsValid) {
            throw ValueValidationException(this)
        }
    }

    companion object {
        private const val PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%!.]).{7,255})"
    }
}
