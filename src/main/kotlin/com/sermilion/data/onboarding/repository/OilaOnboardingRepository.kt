package com.sermilion.data.onboarding.repository

import com.sermilion.data.onboarding.db.model.result.SqlResult
import com.sermilion.domain.onboarding.security.SecurityService
import com.sermilion.domain.onboarding.datasource.UserCredentialsDataSource
import com.sermilion.domain.onboarding.model.registration.value.Email
import com.sermilion.domain.onboarding.model.registration.value.Password
import com.sermilion.domain.onboarding.model.registration.value.Username
import com.sermilion.domain.onboarding.repository.OnboardingRepository
import com.sermilion.domain.onboarding.model.registration.result.RegistrationResult
import com.sermilion.domain.onboarding.model.registration.result.RegistrationResult.RegistrationErrorType
import com.sermilion.domain.onboarding.model.registration.result.RegistrationResult.RegistrationErrorType.UnknownError
import com.sermilion.domain.onboarding.model.registration.result.RegistrationResult.RegistrationErrorType.UsernameOrEmailTaken
import com.sermilion.presentation.routes.model.response.UserResponse
import org.slf4j.LoggerFactory

class OilaOnboardingRepository(
    private val dataSource: UserCredentialsDataSource,
    private val securityService: SecurityService,
) : OnboardingRepository {

    private val logger = LoggerFactory.getLogger(OilaOnboardingRepository::class.java)

    @Suppress("ReturnCount")
    override suspend fun register(
        username: Username,
        password: Password,
        repeatPassword: Password,
        email: Email,
    ): RegistrationResult {
        if (password != repeatPassword) {
            return RegistrationResult.Error(RegistrationErrorType.PasswordMatch)
        }

        val validationErrors = listOf(
            username to RegistrationErrorType.ValidationType.Username,
            password to RegistrationErrorType.ValidationType.Password,
            email to RegistrationErrorType.ValidationType.Email,
        ).filter { (model, _) -> !model.validate() }.map { (_, errorType) -> errorType }

        if (validationErrors.isNotEmpty()) {
            return RegistrationResult.Error(RegistrationErrorType.Validation(validationErrors))
        }

        return try {
            val result = dataSource.createUser(
                username = username.value,
                email = email.value,
                hashedPassword = securityService.hashPassword(password.value),
            )

            when (result) {
                SqlResult.ConstraintViolation -> RegistrationResult.Error(UsernameOrEmailTaken)
                is SqlResult.Success -> {
                    val user = UserResponse(
                        id = result.user.id,
                        username = result.user.username,
                        email = result.user.email,
                    )
                    RegistrationResult.Success(user)
                }
                SqlResult.UnknownError -> RegistrationResult.Error(UnknownError)
            }
        } catch (e: IllegalStateException) {
            logger.error("Unknown error during registration", e)
            RegistrationResult.Error(UnknownError)
        }
    }
}
