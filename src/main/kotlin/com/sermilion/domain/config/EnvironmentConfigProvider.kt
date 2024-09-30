package com.sermilion.domain.config

import com.sermilion.data.onboarding.config.model.JwtConfig

interface EnvironmentConfigProvider {
    fun provideJwtConfig(): JwtConfig
}
