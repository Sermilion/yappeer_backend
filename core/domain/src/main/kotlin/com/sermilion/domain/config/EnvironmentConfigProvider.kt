package com.sermilion.domain.config

import com.sermilion.domain.config.model.JwtConfig

interface EnvironmentConfigProvider {
    fun provideJwtConfig(): JwtConfig
}
