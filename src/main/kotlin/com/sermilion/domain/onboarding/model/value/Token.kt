package com.sermilion.domain.onboarding.model.value

data class Token(
    val value: String,
) {
    init {
        if (value.isEmpty()) {
            throw ValueValidationException(Token::class.simpleName.orEmpty())
        }
    }
}
