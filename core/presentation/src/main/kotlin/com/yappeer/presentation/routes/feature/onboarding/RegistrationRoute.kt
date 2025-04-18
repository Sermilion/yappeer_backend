package com.yappeer.presentation.routes.feature.onboarding

import com.yappeer.domain.onboarding.model.result.RegistrationResult
import com.yappeer.domain.onboarding.model.result.RegistrationResult.RegistrationErrorType
import com.yappeer.domain.onboarding.repository.OnboardingRepository
import com.yappeer.domain.onboarding.security.UserAuthenticationService
import com.yappeer.presentation.routes.model.mapper.UserResponseMapper.toUiModel
import com.yappeer.presentation.routes.model.param.RegisterParams
import com.yappeer.presentation.routes.model.result.ErrorDetail
import com.yappeer.presentation.routes.model.result.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject

internal const val REGISTRATION_ROUTE = "/register"

private const val ERROR_TYPE_REGISTRATION = "RegistrationError"
private const val ERROR_USERNAME_OR_EMAIL_EXISTS = "UsernameOrEmailExistsError"

suspend fun Route.registrationRoute(call: RoutingCall) {
    val onboardingRepository: OnboardingRepository by inject()
    val userAuthenticationService: UserAuthenticationService by inject()
    val request = call.receive<RegisterParams>()

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
                RegistrationErrorType.UsernameOrEmailTaken -> call.respond(
                    status = HttpStatusCode.Conflict,
                    message = ErrorResponse(
                        code = ERROR_USERNAME_OR_EMAIL_EXISTS,
                        details = listOf(
                            ErrorDetail(
                                field = "username or email",
                                detail = "Username or email already taken.",
                            ),
                        ),
                    ),
                )

                else -> call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(code = ERROR_TYPE_REGISTRATION),
                )
            }
        }
    }
}
