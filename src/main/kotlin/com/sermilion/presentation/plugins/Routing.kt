package com.sermilion.presentation.plugins

import com.sermilion.data.onboarding.di.appModule
import com.sermilion.presentation.routes.feature.onboarding.LoginRoute
import com.sermilion.presentation.routes.feature.onboarding.ProfileRoute
import com.sermilion.presentation.routes.feature.onboarding.RefreshTokenRoute
import com.sermilion.presentation.routes.feature.onboarding.RegistrationRoute
import com.sermilion.presentation.routes.feature.onboarding.loginRoute
import com.sermilion.presentation.routes.feature.onboarding.profileRoute
import com.sermilion.presentation.routes.feature.onboarding.refreshTokenRoute
import com.sermilion.presentation.routes.feature.onboarding.registrationRoute
import com.sermilion.presentation.routes.feature.profile.SelfProfileRoute
import com.sermilion.presentation.routes.feature.profile.selfProfileRoute
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.Resources
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.plugin.Koin

fun Application.configureRouting() {
    install(Resources)
    install(Koin) { modules(appModule) }
    configureAuthentication()
    configureErrorHandling()

    routing {
        post(LoginRoute) { loginRoute(call) }
        post(RegistrationRoute) { registrationRoute(call) }

        authenticate(AuthenticationIdentifier) {
            post(ProfileRoute) { profileRoute(call) }
            get(SelfProfileRoute) { selfProfileRoute(call) }
            get(RefreshTokenRoute) { refreshTokenRoute(call) }
        }
    }
}
