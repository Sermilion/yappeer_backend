package com.yappeer.di

import com.yappeer.data.config.YappeerEnvironmentConfigProvider
import com.yappeer.data.onboarding.datasource.YappeerUserDataSource
import com.yappeer.data.onboarding.repository.YappeerOnboardingRepository
import com.yappeer.data.onboarding.security.YappeerJwtTokenService
import com.yappeer.data.onboarding.security.YappeerUserAuthenticationService
import com.yappeer.data.posts.datasource.YappeerPostDataSource
import com.yappeer.data.posts.repository.YappeerPostsRepository
import com.yappeer.data.subscriptions.datasource.YappeerSubscriptionsDataSource
import com.yappeer.data.subscriptions.repository.YappeerSubscriptionsRepository
import com.yappeer.domain.config.EnvironmentConfigProvider
import com.yappeer.domain.onboarding.datasorce.UserDataSource
import com.yappeer.domain.onboarding.repository.OnboardingRepository
import com.yappeer.domain.onboarding.security.JwtTokenService
import com.yappeer.domain.onboarding.security.UserAuthenticationService
import com.yappeer.domain.posts.datasource.PostDataSource
import com.yappeer.domain.posts.repository.PostsRepository
import com.yappeer.domain.subscriptions.datasource.SubscriptionsDataSource
import com.yappeer.domain.subscriptions.repository.SubscriptionsRepository
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDi() {
    val appModule = module {
        single<EnvironmentConfigProvider> { YappeerEnvironmentConfigProvider() }
        single<UserDataSource> { YappeerUserDataSource() }
        single<OnboardingRepository> { YappeerOnboardingRepository(dataSource = get()) }
        single<UserAuthenticationService> { YappeerUserAuthenticationService(configProvider = get()) }
        single<JwtTokenService> { YappeerJwtTokenService() }
        single<SubscriptionsDataSource> { YappeerSubscriptionsDataSource() }
        single<SubscriptionsRepository> { YappeerSubscriptionsRepository(dataSource = get()) }
        single<PostDataSource> { YappeerPostDataSource() }
        single<PostsRepository> { YappeerPostsRepository(dataSource = get()) }
    }

    install(Koin) { modules(appModule) }
}
