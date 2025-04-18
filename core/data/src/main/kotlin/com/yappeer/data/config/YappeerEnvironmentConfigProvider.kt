package com.yappeer.data.config

import com.yappeer.domain.config.EnvironmentConfigProvider
import com.yappeer.domain.config.model.JwtConfig
import com.yappeer.domain.onboarding.security.JwtTokenService

class YappeerEnvironmentConfigProvider(
    private val jwtTokenService: JwtTokenService,
) : EnvironmentConfigProvider {
    override fun provideJwtConfig(): JwtConfig {
        return JwtConfig(secret = jwtTokenService.loadJwtSecret())
    }
}
