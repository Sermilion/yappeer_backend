package com.sermilion.domain.onboarding.security

import com.sermilion.domain.onboarding.model.value.Password
import com.sermilion.domain.onboarding.model.value.Username

interface UserAuthenticationService {
    fun hashPassword(password: Password): Password
    fun verifyPassword(password: Password, hashedPassword: Password): Boolean
    fun generateAccessToken(username: Username): String
    fun generateRefreshToken(username: Username): String
}
