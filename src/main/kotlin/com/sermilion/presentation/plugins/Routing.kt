package com.sermilion.presentation.plugins

import com.sermilion.data.onboarding.di.appModule
import com.sermilion.presentation.routes.RegistrationRoute
import com.sermilion.presentation.routes.registrationRoute
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.plugin.Koin

fun Application.configureRouting() {
    install(Resources)

    install(StatusPages) {
        exception<Exception> { call, cause ->
            call.respondText("App in illegal state as ${cause.message}")
        }
    }

    install(Koin) {
        modules(appModule)
    }

    routing {
        post(RegistrationRoute) {
            registrationRoute(call)
        }
    }
}
