package com.sermilion.data.onboarding.registration.model

data class RegistrationRequest(
    val username: String,
    val password: String,
    val repeatPassword: String,
    val email: String,
)
