package com.yappeer.presentation.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.yappeer.domain.onboarding.security.JwtTokenService
import com.yappeer.domain.onboarding.security.UserAuthenticationService.Companion.CLAIM_EXPIRATION
import com.yappeer.domain.onboarding.security.UserAuthenticationService.Companion.CLAIM_USER_ID
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import org.koin.ktor.ext.inject
import java.util.Date

const val AUTHENTICATION_IDENTIFIER = "auth-jwt"

fun Application.configureAuthentication() {
    val jwtTokenService: JwtTokenService by inject()
    install(Authentication) {
        jwt(AUTHENTICATION_IDENTIFIER) {
            val secret = jwtTokenService.loadJwtSecret()

            verifier(JWT.require(Algorithm.HMAC256(secret)).build())
            validate { credential ->
                if (credential.payload.getClaim(CLAIM_USER_ID).asString().isNotBlank() &&
                    !credential.payload.getClaim(CLAIM_EXPIRATION).asDate().before(Date())
                ) {
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
