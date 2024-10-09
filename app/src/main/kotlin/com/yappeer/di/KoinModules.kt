package com.yappeer.di

import com.yappeer.data.config.OilaEnvironmentConfigProvider
import com.yappeer.data.onboarding.datasource.OilaUserCredentialsDataSource
import com.yappeer.data.onboarding.repository.OilaOnboardingRepository
import com.yappeer.data.onboarding.security.OilaJwtTokenService
import com.yappeer.data.onboarding.security.OilaUserAuthenticationService
import com.yappeer.domain.config.EnvironmentConfigProvider
import com.yappeer.domain.onboarding.datasorce.UserCredentialsDataSource
import com.yappeer.domain.onboarding.repository.OnboardingRepository
import com.yappeer.domain.onboarding.security.JwtTokenService
import com.yappeer.domain.onboarding.security.UserAuthenticationService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDi() {
    val appModule = module {
        single<EnvironmentConfigProvider> { OilaEnvironmentConfigProvider() }
        single<UserCredentialsDataSource> { OilaUserCredentialsDataSource() }
        single<OnboardingRepository> { OilaOnboardingRepository(dataSource = get()) }
        single<UserAuthenticationService> { OilaUserAuthenticationService(configProvider = get()) }
        single<JwtTokenService> { OilaJwtTokenService() }
    }

    install(Koin) { modules(appModule) }
}
