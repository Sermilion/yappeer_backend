package com.yappeer.data.config

import com.yappeer.domain.config.EnvironmentConfigProvider
import com.yappeer.domain.config.model.JwtConfig

class YappeerEnvironmentConfigProvider : EnvironmentConfigProvider {
    override fun provideJwtConfig(): JwtConfig {
        return JwtConfig(secret = System.getenv("jwt.secret"))
    }
}
