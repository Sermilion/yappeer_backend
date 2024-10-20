package com.yappeer.presentation.routes.feature.onboarding

import com.yappeer.domain.onboarding.model.result.RegistrationResult
import com.yappeer.domain.onboarding.model.result.RegistrationResult.RegistrationErrorType.UsernameOrEmailTaken
import com.yappeer.domain.onboarding.model.value.ValueValidationException
import com.yappeer.domain.onboarding.repository.OnboardingRepository
import com.yappeer.domain.onboarding.security.UserAuthenticationService
import com.yappeer.presentation.routes.model.mapper.UserResponseMapper.toUiModel
import com.yappeer.presentation.routes.model.mapper.toPresentationModel
import com.yappeer.presentation.routes.model.param.RegisterParams
import com.yappeer.presentation.routes.model.result.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

internal const val RegistrationRoute = "/register"
private const val ErrorTypeValidation = "ValidationError"
private const val ErrorTypePasswordMatch = "PasswordMatch"

suspend fun Route.registrationRoute(call: RoutingCall) {
    val onboardingRepository: OnboardingRepository by inject()
    val userAuthenticationService: UserAuthenticationService by inject()
    val logger = LoggerFactory.getLogger(RegistrationRoute::class.java)
    val request = call.receive<RegisterParams>()

    try {
        val password = request.passwordValue
        val repeatPassword = request.repeatPasswordValue

        if (password != repeatPassword) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(code = ErrorTypePasswordMatch, details = emptyList()),
            )
        }

        val result = onboardingRepository.register(
            username = request.usernameValue,
            hashedPassword = userAuthenticationService.hashPassword(password),
            email = request.emailValue,
        )

        when (result) {
            is RegistrationResult.Success -> {
                call.respond(HttpStatusCode.Created, result.user.toUiModel())
            }
            is RegistrationResult.Error -> {
                when (result.errorType) {
                    UsernameOrEmailTaken -> call.respond(
                        status = HttpStatusCode.Conflict,
                        message = "Username or email already taken.",
                    )

                    else -> call.respond(HttpStatusCode.InternalServerError, "Registration failed.")
                }
            }
        }
    } catch (e: ValueValidationException) {
        logger.info("Validation error for value type ${e.valueType}", e)
        val response = ErrorResponse(
            code = ErrorTypeValidation,
            details = listOf(e.valueType).toPresentationModel(),
        )
        call.respond(HttpStatusCode.BadRequest, response)
    }
}
