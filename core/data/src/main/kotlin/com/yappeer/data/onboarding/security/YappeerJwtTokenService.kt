package com.yappeer.data.onboarding.security

import com.yappeer.domain.onboarding.security.JwtTokenService

class YappeerJwtTokenService : JwtTokenService {
    override fun loadJwtSecret(): String {
        return System.getenv(JWT_SECRET_KEY)
    }

    private companion object {
        const val JWT_SECRET_KEY = "jwtsecret"
    }
}
