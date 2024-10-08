package com.sermilion.di

import com.sermilion.data.config.OilaEnvironmentConfigProvider
import com.sermilion.data.onboarding.datasource.OilaUserCredentialsDataSource
import com.sermilion.data.onboarding.repository.OilaOnboardingRepository
import com.sermilion.data.onboarding.security.OilaJwtTokenService
import com.sermilion.data.onboarding.security.OilaUserAuthenticationService
import com.sermilion.domain.config.EnvironmentConfigProvider
import com.sermilion.domain.onboarding.datasorce.UserCredentialsDataSource
import com.sermilion.domain.onboarding.repository.OnboardingRepository
import com.sermilion.domain.onboarding.security.JwtTokenService
import com.sermilion.domain.onboarding.security.UserAuthenticationService
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
