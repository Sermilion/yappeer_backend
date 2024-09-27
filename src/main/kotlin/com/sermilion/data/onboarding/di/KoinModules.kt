package com.sermilion.data.onboarding.di

import com.sermilion.data.onboarding.datasource.OilaUserCredentialsDataSource
import org.koin.dsl.module
import com.sermilion.data.onboarding.repository.OilaOnboardingRepository // Import your repository
import com.sermilion.domain.onboarding.datasource.UserCredentialsDataSource
import com.sermilion.domain.onboarding.repository.OnboardingRepository

val appModule = module {
    single<UserCredentialsDataSource> { OilaUserCredentialsDataSource() }
    single<OnboardingRepository> { OilaOnboardingRepository(dataSource = get()) }
}