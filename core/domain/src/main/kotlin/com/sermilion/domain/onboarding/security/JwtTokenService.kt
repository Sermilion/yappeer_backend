package com.sermilion.domain.onboarding.security

interface JwtTokenService {
    fun loadJwtSecret(): String
}
