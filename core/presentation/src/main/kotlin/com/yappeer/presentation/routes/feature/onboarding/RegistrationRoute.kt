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

internal const val REGISTRATION_ROUTE = "/register"
private const val ERROR_TYPE_VALIDATION = "ValidationError"

suspend fun Route.registrationRoute(call: RoutingCall) {
    val onboardingRepository: OnboardingRepository by inject()
    val userAuthenticationService: UserAuthenticationService by inject()
    val logger = LoggerFactory.getLogger(REGISTRATION_ROUTE::class.java)
    val request = call.receive<RegisterParams>()

    try {
        val result = onboardingRepository.register(
            username = request.usernameValue,
            hashedPassword = userAuthenticationService.hashPassword(request.passwordValue),
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
            code = ERROR_TYPE_VALIDATION,
            details = listOf(e.valueType).toPresentationModel(),
        )
        call.respond(HttpStatusCode.BadRequest, response)
    }
}
