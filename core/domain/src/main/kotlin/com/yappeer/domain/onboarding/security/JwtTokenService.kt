package com.yappeer.domain.onboarding.security

interface JwtTokenService {
    fun loadJwtSecret(): String
}
