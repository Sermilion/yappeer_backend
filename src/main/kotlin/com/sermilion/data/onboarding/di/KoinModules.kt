package com.sermilion.data.onboarding.di

import com.sermilion.data.onboarding.OilaSecurityService
import com.sermilion.data.onboarding.datasource.OilaUserCredentialsDataSource
import com.sermilion.data.onboarding.repository.OilaOnboardingRepository // Import your repository
import com.sermilion.domain.onboarding.SecurityService
import com.sermilion.domain.onboarding.datasource.UserCredentialsDataSource
import com.sermilion.domain.onboarding.repository.OnboardingRepository
import org.koin.dsl.module

val appModule = module {
    single<UserCredentialsDataSource> { OilaUserCredentialsDataSource() }
    single<OnboardingRepository> {
        OilaOnboardingRepository(
            dataSource = get(),
            securityService = get(),
        )
    }
    single<SecurityService> { OilaSecurityService() }
}
