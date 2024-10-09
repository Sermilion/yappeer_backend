package com.yappeer.data.onboarding.repository

import com.yappeer.domain.onboarding.datasorce.UserCredentialsDataSource
import com.yappeer.domain.onboarding.model.Profile
import com.yappeer.domain.onboarding.model.result.RegistrationResult
import com.yappeer.domain.onboarding.model.result.RegistrationResult.RegistrationErrorType.UnknownError
import com.yappeer.domain.onboarding.model.result.RegistrationResult.RegistrationErrorType.UsernameOrEmailTaken
import com.yappeer.domain.onboarding.model.result.SqlRegistrationResult
import com.yappeer.domain.onboarding.model.value.Email
import com.yappeer.domain.onboarding.model.value.Password
import com.yappeer.domain.onboarding.model.value.Username
import com.yappeer.domain.onboarding.repository.OnboardingRepository

class OilaOnboardingRepository(
    private val dataSource: UserCredentialsDataSource,
) : OnboardingRepository {

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
                val user = RegistrationResult.Data(
                    id = result.user.id.toString(),
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
        return dataSource.findPassword(username.value)
    }

    override fun findUser(username: Username): Profile? {
        val result = dataSource.findUser(username.value.lowercase())
        val profile = result?.let {
            Profile(
                id = it.id,
                username = it.username,
                email = it.email,
                avatar = it.avatar,
            )
        }
        return profile
    }
}
