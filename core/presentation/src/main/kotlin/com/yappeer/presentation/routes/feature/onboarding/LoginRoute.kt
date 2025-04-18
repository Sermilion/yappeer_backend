package com.yappeer.presentation.routes.feature.onboarding

import com.yappeer.domain.onboarding.repository.OnboardingRepository
import com.yappeer.domain.onboarding.security.UserAuthenticationService
import com.yappeer.presentation.routes.model.param.LoginParams
import com.yappeer.presentation.routes.model.result.ErrorResponse
import com.yappeer.presentation.routes.model.ui.TokenUiModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import kotlinx.datetime.Clock
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

internal const val LOGIN_ROUTE = "/login"
private const val ERROR_TYPE_INVALID_CREDENTIALS = "InvalidCredentials"

suspend fun Route.loginRoute(call: RoutingCall) {
    val onboardingRepository: OnboardingRepository by inject()
    val userAuthenticationService: UserAuthenticationService by inject()

    val logger = LoggerFactory.getLogger(LOGIN_ROUTE)
    val params = call.receive<LoginParams>()

    val passwordHash = onboardingRepository.findPassword(params.emailValue)
        ?: return call.respond(HttpStatusCode.BadRequest, createInvalidCredentialsError())

    val user = onboardingRepository.findUser(params.emailValue)

    val passwordMatches = userAuthenticationService.verifyPassword(params.passwordValue, passwordHash)
    if (passwordMatches && user != null) {
        val accessToken = userAuthenticationService.generateAccessToken(user.id)
        val refreshToken = userAuthenticationService.generateRefreshToken(user.id)
        val response = TokenUiModel(accessToken = accessToken, refreshToken = refreshToken)
        onboardingRepository.updateLastLogin(user.id, Clock.System.now())
        call.respond(HttpStatusCode.OK, response)
    } else {
        logger.info("Invalid credentials for email: ${params.emailValue}.")
        call.respond(HttpStatusCode.BadRequest, createInvalidCredentialsError())
    }
}

private fun createInvalidCredentialsError(): ErrorResponse {
    return ErrorResponse(
        code = ERROR_TYPE_INVALID_CREDENTIALS,
        details = emptyList(),
    )
}
