package com.sermilion.domain.onboarding.model.registration.value

import java.util.regex.Pattern

@JvmInline
value class Password(val value: String) : Validateable {

    override fun validate(): Boolean {
        return value.isNotEmpty() && Pattern.compile(PASSWORD_PATTERN).matcher(value).matches()
    }

    companion object {
        private const val PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%!]).{7,255})"
    }
}
