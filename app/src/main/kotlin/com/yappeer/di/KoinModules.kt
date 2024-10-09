package com.yappeer.di

import com.yappeer.data.config.YappeerEnvironmentConfigProvider
import com.yappeer.data.onboarding.datasource.YappeerUserCredentialsDataSource
import com.yappeer.data.onboarding.repository.YappeerOnboardingRepository
import com.yappeer.data.onboarding.security.YappeerJwtTokenService
import com.yappeer.data.onboarding.security.YappeerUserAuthenticationService
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
        single<EnvironmentConfigProvider> { YappeerEnvironmentConfigProvider() }
        single<UserCredentialsDataSource> { YappeerUserCredentialsDataSource() }
        single<OnboardingRepository> { YappeerOnboardingRepository(dataSource = get()) }
        single<UserAuthenticationService> { YappeerUserAuthenticationService(configProvider = get()) }
        single<JwtTokenService> { YappeerJwtTokenService() }
    }

    install(Koin) { modules(appModule) }
}
