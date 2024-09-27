package com.sermilion.data.onboarding.repository

import com.sermilion.data.onboarding.db.model.result.SQLResult
import com.sermilion.domain.onboarding.datasource.UserCredentialsDataSource
import com.sermilion.domain.onboarding.repository.OnboardingRepository
import com.sermilion.domain.onboarding.repository.OnboardingRepository.RegistrationResult
import com.sermilion.domain.onboarding.repository.OnboardingRepository.RegistrationType
import java.security.SecureRandom
import java.util.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory

class OilaOnboardingRepository(
    private val dataSource: UserCredentialsDataSource
) : OnboardingRepository {

    private val logger = LoggerFactory.getLogger(OilaOnboardingRepository::class.java)

    override suspend fun register(
        username: String,
        password: String,
        repeatPassword: String,
        email: String,
    ): RegistrationResult {
        if (password != repeatPassword) {
            return RegistrationResult.Error(RegistrationType.PasswordMatch)
        }

        return try {
            val salt = generateSalt()
            val hashedPassword = hashPassword(password, salt)
            val result = dataSource.createUser(
                username = username,
                email = email,
                salt = salt,
                hashedPassword = hashedPassword,
            )

            when (result) {
                SQLResult.ConstraintViolation -> RegistrationResult.Error(RegistrationType.UsernameOrEmailTaken)
                SQLResult.Success -> RegistrationResult.Success
                SQLResult.UnknownError -> RegistrationResult.Error(RegistrationType.UnknownError)
            }
        } catch (e: Exception) {
            logger.error("Unknown error during registration", e)
            RegistrationResult.Error(RegistrationType.UnknownError)
        }
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    private fun hashPassword(password: String, salt: String): String {
        val saltedPassword = password + salt
        return DigestUtils.sha256Hex(saltedPassword)
    }
}