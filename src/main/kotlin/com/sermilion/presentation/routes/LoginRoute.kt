package com.sermilion.presentation.routes

import com.sermilion.domain.onboarding.model.value.ValueValidationException
import com.sermilion.domain.onboarding.repository.OnboardingRepository
import com.sermilion.domain.onboarding.security.SecurityService
import com.sermilion.presentation.routes.model.param.LoginParams
import com.sermilion.presentation.routes.model.response.ErrorResponse
import com.sermilion.presentation.routes.model.response.LoginResponseModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

internal const val LoginRoute = "/login"
private const val ErrorTypeInvalidCredentials = "InvalidCredentials"

suspend fun Route.loginRoute(call: RoutingCall) {
    val onboardingRepository: OnboardingRepository by inject()
    val securityService: SecurityService by inject()

    val logger = LoggerFactory.getLogger(LoginRoute)
    val params = call.receive<LoginParams>()

    try {
        val passwordHash = onboardingRepository
            .findPassword(params.usernameValue)
            ?: return call.respond(HttpStatusCode.BadRequest, createInvalidCredentialsError())

        val passwordMatches = securityService.verifyPassword(params.passwordValue, passwordHash)
        if (passwordMatches) {
            val accessToken = securityService.generateAccessToken(username = params.usernameValue)
            val refreshToken = securityService.generateRefreshToken(params.usernameValue)
            val response = LoginResponseModel(accessToken = accessToken, refreshToken = refreshToken)
            call.respond(HttpStatusCode.OK, response)
        } else {
            call.respond(HttpStatusCode.BadRequest, createInvalidCredentialsError())
        }
    } catch (e: ValueValidationException) {
        logger.error("Validation error for value type ${e.valueType}", e)
        call.respond(HttpStatusCode.BadRequest, createInvalidCredentialsError())
    }
}

private fun createInvalidCredentialsError(): ErrorResponse {
    return ErrorResponse(
        code = ErrorTypeInvalidCredentials,
        details = emptyList(),
    )
}
