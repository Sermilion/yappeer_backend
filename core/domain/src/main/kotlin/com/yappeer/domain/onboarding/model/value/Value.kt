package com.yappeer.domain.onboarding.model.value

interface Value {
    val name: String get() = this::class.simpleName.orEmpty().lowercase()
    val errorMessage: String
}
