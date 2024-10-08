package com.sermilion.data.registration.model

data class RegistrationRequestDataModel(
    val username: String,
    val password: String,
    val repeatPassword: String,
    val email: String,
)
