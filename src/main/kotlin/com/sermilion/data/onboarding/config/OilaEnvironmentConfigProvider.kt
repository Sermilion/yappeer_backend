package com.sermilion.data.onboarding.config

import com.sermilion.data.onboarding.config.model.JwtConfig
import com.sermilion.domain.config.EnvironmentConfigProvider

class OilaEnvironmentConfigProvider : EnvironmentConfigProvider {
    override fun provideJwtConfig(): JwtConfig {
        return JwtConfig(secret = System.getenv("jwt.secret"))
    }
}
