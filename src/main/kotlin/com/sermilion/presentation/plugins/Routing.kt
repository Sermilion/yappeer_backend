package com.sermilion.presentation.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.sermilion.data.onboarding.di.appModule
import com.sermilion.presentation.routes.RegistrationRoute
import com.sermilion.presentation.routes.loginRoute
import com.sermilion.presentation.routes.registrationRoute
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.h2.util.SortedProperties.loadProperties
import org.koin.ktor.plugin.Koin
import org.slf4j.LoggerFactory

fun Application.configureRouting() {
    val logger = LoggerFactory.getLogger("Routing")

    install(Resources)

    install(StatusPages) {
        exception<Exception> { call, cause ->
            val message = "App in illegal state as ${cause.message}"
            logger.error(message, cause)
            call.respond(HttpStatusCode.InternalServerError, message)
        }
    }

    install(Koin) {
        modules(appModule)
    }

    install(Authentication) {
        jwt("auth-jwt") {
            val properties = loadProperties("local.properties")
            val secret = properties.getProperty("jwt.secret")
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .build(),
            )

            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }

    routing {
        post("/login") {
            loginRoute(call)
        }

        post(RegistrationRoute) {
            registrationRoute(call)
        }
    }
}
