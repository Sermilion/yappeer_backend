package com.sermilion.data.onboarding.security

import com.sermilion.domain.onboarding.security.SecurityService
import de.mkammerer.argon2.Argon2Factory

class OilaSecurityService : SecurityService {

    private val argon2 = Argon2Factory.create()

    override fun hashPassword(password: String): String {
        return argon2.hash(
            ITERATIONS,
            MEMORY_KB,
            PARALLELISM,
            password.toCharArray(),
        )
    }

    override fun verifyPassword(password: String, hashedPassword: String): Boolean {
        try {
            return argon2.verify(hashedPassword, password)
        } finally {
            argon2.wipeArray(password.toCharArray())
        }
    }

    companion object {
        private const val ITERATIONS = 10
        private const val MEMORY_KB = 65536 // 64 MB
        private const val PARALLELISM = 1
    }
}
