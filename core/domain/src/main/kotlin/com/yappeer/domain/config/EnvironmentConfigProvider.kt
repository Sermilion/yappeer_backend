package com.yappeer.domain.config

import com.yappeer.domain.config.model.JwtConfig

interface EnvironmentConfigProvider {
    fun provideJwtConfig(): JwtConfig
}
