package com.sermilion.data.onboarding.di

import com.sermilion.data.onboarding.config.OilaEnvironmentConfigProvider
import com.sermilion.data.onboarding.datasource.OilaUserCredentialsDataSource
import com.sermilion.data.onboarding.repository.OilaOnboardingRepository
import com.sermilion.data.onboarding.security.OilaJwtTokenService
import com.sermilion.data.onboarding.security.OilaUserAuthenticationService
import com.sermilion.domain.config.EnvironmentConfigProvider
import com.sermilion.domain.onboarding.datasource.UserCredentialsDataSource
import com.sermilion.domain.onboarding.repository.OnboardingRepository
import com.sermilion.domain.onboarding.security.JwtTokenService
import com.sermilion.domain.onboarding.security.UserAuthenticationService
import org.koin.dsl.module

val appModule = module {
    single<EnvironmentConfigProvider> { OilaEnvironmentConfigProvider() }
    single<UserCredentialsDataSource> { OilaUserCredentialsDataSource() }
    single<OnboardingRepository> { OilaOnboardingRepository(dataSource = get()) }
    single<UserAuthenticationService> { OilaUserAuthenticationService(configProvider = get()) }
    single<JwtTokenService> { OilaJwtTokenService() }
}
