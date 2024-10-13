package com.yappeer.data.onboarding.repository

import com.yappeer.domain.onboarding.datasorce.UserDataSource
import com.yappeer.domain.onboarding.model.User
import com.yappeer.domain.onboarding.model.result.RegistrationResult
import com.yappeer.domain.onboarding.model.result.RegistrationResult.RegistrationErrorType.UnknownError
import com.yappeer.domain.onboarding.model.result.RegistrationResult.RegistrationErrorType.UsernameOrEmailTaken
import com.yappeer.domain.onboarding.model.result.SqlRegistrationResult
import com.yappeer.domain.onboarding.model.value.Email
import com.yappeer.domain.onboarding.model.value.Password
import com.yappeer.domain.onboarding.model.value.Username
import com.yappeer.domain.onboarding.repository.OnboardingRepository
import kotlinx.datetime.Instant
import java.util.UUID

class YappeerOnboardingRepository(
    private val dataSource: UserDataSource,
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
            is SqlRegistrationResult.Success -> RegistrationResult.Success(result.user)
            SqlRegistrationResult.UnknownError -> RegistrationResult.Error(UnknownError)
        }
    }

    override fun findPassword(email: Email): Password? {
        return dataSource.findPassword(email)
    }

    override fun findUser(userId: UUID): User? {
        return dataSource.findUser(userId)
    }

    override fun findUser(email: Email): User? {
        return dataSource.findUser(email)
    }

    override fun updateLastLogin(userId: UUID, instant: Instant) {
        dataSource.updateLastLogin(userId, instant)
    }
}
