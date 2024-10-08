package com.sermilion.data.onboarding.security

import com.sermilion.domain.onboarding.security.JwtTokenService
import org.h2.util.SortedProperties.loadProperties

class OilaJwtTokenService : JwtTokenService {
    override fun loadJwtSecret(): String {
        val properties = loadProperties("local.properties")
        return properties.getProperty("jwt.secret")
    }
}
