package com.yappeer.presentation.routes.feature.onboarding

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.yappeer.domain.onboarding.model.value.Username
import com.yappeer.domain.onboarding.model.value.ValueValidationException
import com.yappeer.domain.onboarding.security.JwtTokenService
import com.yappeer.domain.onboarding.security.UserAuthenticationService
import com.yappeer.presentation.routes.model.TokenUiModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.util.Date

internal const val RefreshTokenRoute = "/refresh_token"
private const val HeaderAuthorization = "Authorization"
private const val HeaderBearer = "Bearer "
private const val JwtClaimUsername = "username"

suspend fun Route.refreshTokenRoute(call: RoutingCall) {
    val userAuthenticationService: UserAuthenticationService by inject()
    val tokeService: JwtTokenService by inject()

    val logger = LoggerFactory.getLogger(RefreshTokenRoute)

    val jwtSecret = tokeService.loadJwtSecret()

    try {
        val refreshToken = call.request.header(HeaderAuthorization)?.substringAfter(HeaderBearer)
        if (refreshToken != null) {
            // use ECDSA512 instead
            val decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret))
                .build()
                .verify(refreshToken)

            val username = decodedJWT.claims[JwtClaimUsername]?.asString()?.let {
                Username(it)
            }

            if (decodedJWT.expiresAt.before(Date()) || username == null) {
                call.respond(HttpStatusCode.Unauthorized)
            } else {
                val newAccessToken = userAuthenticationService.generateAccessToken(username)
                val newRefreshToken = userAuthenticationService.generateRefreshToken(username)
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
