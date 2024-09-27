package com.sermilion.domain.onboarding

interface SecurityService {
    fun hashPassword(password: String): String
    fun verifyPassword(password: String, hashedPassword: String): Boolean
}
