package com.sermilion.data.onboarding.model1.registration

data class RegistrationRequest(
    val username: String,
    val password: String,
    val repeatPassword: String,
    val email: String,
)
