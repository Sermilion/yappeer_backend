package com.yappeer.presentation.plugins

import com.yappeer.presentation.routes.feature.onboarding.LoginRoute
import com.yappeer.presentation.routes.feature.onboarding.ProfileRoute
import com.yappeer.presentation.routes.feature.onboarding.RefreshTokenRoute
import com.yappeer.presentation.routes.feature.onboarding.RegistrationRoute
import com.yappeer.presentation.routes.feature.onboarding.loginRoute
import com.yappeer.presentation.routes.feature.onboarding.profileRoute
import com.yappeer.presentation.routes.feature.onboarding.refreshTokenRoute
import com.yappeer.presentation.routes.feature.onboarding.registrationRoute
import com.yappeer.presentation.routes.feature.profile.SelfProfileRoute
import com.yappeer.presentation.routes.feature.profile.selfProfileRoute
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.Resources
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    install(Resources)
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
