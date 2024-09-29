package com.sermilion.presentation.routes

import com.sermilion.domain.onboarding.repository.OnboardingRepository
import com.sermilion.domain.onboarding.model.registration.result.RegistrationResult
import com.sermilion.domain.onboarding.model.registration.result.RegistrationResult.RegistrationErrorType
import com.sermilion.presentation.routes.model.response.ErrorResponse
import com.sermilion.presentation.routes.model.param.RegisterParams
import com.sermilion.presentation.routes.model.mapper.toPresentationModel
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

internal const val RegistrationRoute = "/register"
private const val ErrorTypeValidation = "validation_error"

suspend fun Route.registrationRoute(call: RoutingCall) {
    val onboardingRepository: OnboardingRepository by inject()

    val logger = LoggerFactory.getLogger(RegistrationRoute::class.java)

    val request = call.receive<RegisterParams>()
    try {
        val result = onboardingRepository.register(
            username = request.usernameValue,
            password = request.passwordValue,
            repeatPassword = request.repeatPasswordValue,
            email = request.emailValue,
        )

        when (result) {
            is RegistrationResult.Success -> call.respond(HttpStatusCode.Created, result.user)

            is RegistrationResult.Error -> {
                when (result.errorType) {
                    RegistrationErrorType.PasswordMatch -> {
                        call.respond(HttpStatusCode.BadRequest, "Passwords do not match.")
                    }

                    RegistrationErrorType.UsernameOrEmailTaken -> {
                        call.respond(HttpStatusCode.Conflict, "Username or email already taken.")
                    }
                    is RegistrationErrorType.Validation -> {
                        val validationErrors = result.errorType.validationErrors.toPresentationModel()
                        val response = ErrorResponse(
                            code = ErrorTypeValidation,
                            details = validationErrors,
                        )
                        call.respond(HttpStatusCode.BadRequest, response)
                    }

                    else -> {
                        call.respond(HttpStatusCode.InternalServerError, "Registration failed.")
                    }
                }
            }
        }
    } catch (ex: IllegalStateException) {
        logger.error("Error while registering new user", ex)
        call.respond(HttpStatusCode.BadRequest)
    } catch (ex: JsonConvertException) {
        logger.error("Error while registering new user", ex)
        call.respond(HttpStatusCode.BadRequest)
    }
}

