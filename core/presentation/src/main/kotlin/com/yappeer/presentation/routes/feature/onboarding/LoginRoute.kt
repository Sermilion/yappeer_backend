package com.yappeer.presentation.routes.feature.onboarding

import com.yappeer.domain.onboarding.model.value.maskForLogging
import com.yappeer.domain.onboarding.repository.OnboardingRepository
import com.yappeer.domain.onboarding.security.UserAuthenticationService
import com.yappeer.presentation.routes.model.param.LoginParams
import com.yappeer.presentation.routes.model.result.ErrorDetail
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
import java.util.UUID

internal const val LOGIN_ROUTE = "/login"
internal const val ERROR_TYPE_INVALID_CREDENTIALS = "InvalidCredentials"
internal const val ERROR_TYPE_VALIDATION_ERROR = "ValidationError"
internal const val ERROR_TYPE_ACCOUNT_LOCKED = "AccountLocked"

suspend fun Route.loginRoute(call: RoutingCall) {
    val onboardingRepository: OnboardingRepository by inject()
    val userAuthenticationService: UserAuthenticationService by inject()

    val logger = LoggerFactory.getLogger(LOGIN_ROUTE)
    val params = call.receive<LoginParams>()

    logger.info("Login attempt for email: ${params.emailValue.maskForLogging()}")

    // Check for too many failed attempts
    if (userAuthenticationService.isAccountLocked(params.emailValue)) {
        logger.warn("Account locked for email: ${params.emailValue.maskForLogging()}")
        call.respond(
            HttpStatusCode.TooManyRequests,
            ErrorResponse(
                code = ERROR_TYPE_ACCOUNT_LOCKED,
                details = listOf(ErrorDetail("login", "Too many failed login attempts")),
                message = "Account temporarily locked. Please try again later.",
            ),
        )
        return
    }

    // Optimized to fetch user in a single query
    val userWithPassword = onboardingRepository.findUserWithPassword(params.emailValue)

    if (userWithPassword == null) {
        handleFailedLogin(call, userAuthenticationService, params, logger, null)
        return
    }

    val passwordMatches = userAuthenticationService.verifyPassword(
        params.passwordValue,
        userWithPassword.passwordValue,
    )
    if (!passwordMatches) {
        handleFailedLogin(call, userAuthenticationService, params, logger, userWithPassword.id)
    } else {
        // Login successful
        userAuthenticationService.clearFailedAttempts(params.emailValue)
        val accessToken = userAuthenticationService.generateAccessToken(userWithPassword.id)
        val refreshToken = userAuthenticationService.generateRefreshToken(userWithPassword.id)
        val response = TokenUiModel(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = userAuthenticationService.getAccessTokenExpiration(),
        )

        onboardingRepository.updateLastLogin(userWithPassword.id, Clock.System.now())
        logger.info("Login successful for user ID: ${userWithPassword.id}")
        call.respond(HttpStatusCode.OK, response)
    }
}

private suspend fun handleFailedLogin(
    call: RoutingCall,
    userAuthenticationService: UserAuthenticationService,
    params: LoginParams,
    logger: org.slf4j.Logger,
    userId: UUID?,
) {
    userAuthenticationService.recordFailedLoginAttempt(params.emailValue)
    logger.info(
        "Invalid credentials for email: " +
            "${params.emailValue.maskForLogging()}${userId?.let { ", user ID: $it" } ?: ""}",
    )
    call.respond(HttpStatusCode.Unauthorized, createInvalidCredentialsError())
}

private fun createInvalidCredentialsError(): ErrorResponse {
    return ErrorResponse(
        code = ERROR_TYPE_INVALID_CREDENTIALS,
        details = listOf(ErrorDetail("credentials", "The email or password you entered is incorrect")),
        message = "Invalid login credentials",
    )
}
