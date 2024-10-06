package com.sermilion.domain.onboarding.model.value

import java.util.regex.Pattern

@JvmInline
value class Password(val value: String) {

    init {
        val passwordIsValid = value.isNotEmpty() && Pattern.compile(PASSWORD_PATTERN).matcher(value).matches()
        if (!passwordIsValid) {
            throw ValueValidationException(Password::class.simpleName.orEmpty())
        }
    }

    companion object {
        private const val PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%!]).{7,255})"
    }
}
