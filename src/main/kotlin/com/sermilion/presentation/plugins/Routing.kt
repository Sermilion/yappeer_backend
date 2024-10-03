package com.sermilion.presentation.plugins

import com.sermilion.data.onboarding.di.appModule
import com.sermilion.presentation.routes.LoginRoute
import com.sermilion.presentation.routes.ProfileRoute
import com.sermilion.presentation.routes.RegistrationRoute
import com.sermilion.presentation.routes.SelfProfileRoute
import com.sermilion.presentation.routes.loginRoute
import com.sermilion.presentation.routes.profileRoute
import com.sermilion.presentation.routes.registrationRoute
import com.sermilion.presentation.routes.selfProfileRoute
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
        }
    }
}
