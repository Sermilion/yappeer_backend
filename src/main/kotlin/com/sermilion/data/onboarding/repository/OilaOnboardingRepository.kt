package com.sermilion.data.onboarding.repository

import com.sermilion.data.onboarding.db.model.result.SqlRegistrationResult
import com.sermilion.domain.onboarding.datasource.UserCredentialsDataSource
import com.sermilion.domain.onboarding.model.registration.result.RegistrationResult
import com.sermilion.domain.onboarding.model.registration.result.RegistrationResult.RegistrationErrorType.UnknownError
import com.sermilion.domain.onboarding.model.registration.result.RegistrationResult.RegistrationErrorType.UsernameOrEmailTaken
import com.sermilion.domain.onboarding.model.value.Email
import com.sermilion.domain.onboarding.model.value.Password
import com.sermilion.domain.onboarding.model.value.Username
import com.sermilion.domain.onboarding.repository.OnboardingRepository
import com.sermilion.presentation.routes.model.response.RegistrationResponseModel
import org.slf4j.LoggerFactory
import java.sql.SQLException

class OilaOnboardingRepository(
    private val dataSource: UserCredentialsDataSource,
) : OnboardingRepository {

    private val logger = LoggerFactory.getLogger(OilaOnboardingRepository::class.java)

    override fun register(
        username: Username,
        hashedPassword: Password,
        email: Email,
    ): RegistrationResult {
        val result = dataSource.createUser(
            username = username.value,
            email = email.value,
            hashedPassword = hashedPassword.value,
        )

        return when (result) {
            SqlRegistrationResult.ConstraintViolation -> RegistrationResult.Error(UsernameOrEmailTaken)
            is SqlRegistrationResult.Success -> {
                val user = RegistrationResponseModel(
                    id = result.user.id,
                    username = result.user.username,
                    email = result.user.email,
                    avatar = result.user.avatar,
                )
                RegistrationResult.Success(user)
            }

            SqlRegistrationResult.UnknownError -> RegistrationResult.Error(UnknownError)
        }
    }

    override fun findPassword(username: Username): Password? {
        return try {
            dataSource.findPassword(username.value)
        } catch (e: SQLException) {
            logger.info("Exception while finding password for username: `$username`", e)
            null
        }
    }
}
