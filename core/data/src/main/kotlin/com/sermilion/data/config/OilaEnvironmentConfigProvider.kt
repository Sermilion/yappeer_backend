package com.sermilion.data.config

import com.sermilion.domain.config.EnvironmentConfigProvider
import com.sermilion.domain.config.model.JwtConfig

class OilaEnvironmentConfigProvider : EnvironmentConfigProvider {
    override fun provideJwtConfig(): JwtConfig {
        return JwtConfig(secret = System.getenv("jwt.secret"))
    }
}
