package com.sermilion.presentation.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import org.h2.util.SortedProperties.loadProperties

const val AuthenticationIdentifier = "auth-jwt"

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt(AuthenticationIdentifier) {
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
}
