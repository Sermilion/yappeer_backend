package com.yappeer.presentation.routes.feature.onboarding

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.yappeer.domain.onboarding.model.value.ValueValidationException
import com.yappeer.domain.onboarding.security.JwtTokenService
import com.yappeer.domain.onboarding.security.UserAuthenticationService
import com.yappeer.domain.onboarding.security.UserAuthenticationService.Companion.CLAIM_USER_ID
import com.yappeer.presentation.routes.model.ui.TokenUiModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.util.Date
import java.util.UUID

internal const val REFRESH_TOKEN_ROUTE = "/refresh_token"
private const val HEADER_AUTHORIZATION = "Authorization"
private const val HEADER_BEARER = "Bearer "

suspend fun Route.refreshTokenRoute(call: RoutingCall) {
    val userAuthenticationService: UserAuthenticationService by inject()
    val tokeService: JwtTokenService by inject()

    val logger = LoggerFactory.getLogger(REFRESH_TOKEN_ROUTE)

    val jwtSecret = tokeService.loadJwtSecret()

    try {
        val refreshToken = call.request.header(HEADER_AUTHORIZATION)?.substringAfter(HEADER_BEARER)
        if (refreshToken != null) {
            // use ECDSA512 instead
            val decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret))
                .build()
                .verify(refreshToken)

            val userId = decodedJWT.claims[CLAIM_USER_ID]?.asString()?.let {
                UUID.fromString(it)
            }

            if (decodedJWT.expiresAt.before(Date()) || userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
            } else {
                val newAccessToken = userAuthenticationService.generateAccessToken(userId)
                val newRefreshToken = userAuthenticationService.generateRefreshToken(userId)
                call.respond(
                    HttpStatusCode.OK,
                    TokenUiModel(
                        accessToken = newAccessToken,
                        refreshToken = newRefreshToken,
                    ),
                )
            }
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    } catch (exception: ValueValidationException) {
        logger.error("Error refreshing token", exception)
        call.respond(HttpStatusCode.Unauthorized)
    } catch (e: JWTVerificationException) {
        logger.error("Error refreshing token", e)
        call.respond(HttpStatusCode.Unauthorized)
    }
}
