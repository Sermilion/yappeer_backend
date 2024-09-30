package com.sermilion.data.onboarding.model.registration

data class RegistrationRequestDataModel(
    val username: String,
    val password: String,
    val repeatPassword: String,
    val email: String,
)
