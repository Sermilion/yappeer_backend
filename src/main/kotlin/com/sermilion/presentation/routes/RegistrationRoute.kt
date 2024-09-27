package com.sermilion.presentation.routes

import com.sermilion.domain.onboarding.repository.OnboardingRepository
import com.sermilion.domain.onboarding.repository.OnboardingRepository.RegistrationResult
import com.sermilion.domain.onboarding.repository.OnboardingRepository.RegistrationType
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.serialization.JsonConvertException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject

@Resource("register")
class Register(
    val username: String,
    val password: String,
    val repeatPassword: String,
    val email: String,
)

internal const val RegistrationRoute = "/register"

suspend fun Route.registrationRoute(call: RoutingCall) {
    val onboardingRepository: OnboardingRepository by inject()

    val request = call.receive<Register>()
    try {
        val result = onboardingRepository.register(
            username = request.username,
            password = request.password,
            repeatPassword = request.repeatPassword,
            email = request.email
        )

        when (result) {
            is RegistrationResult.Success -> call.respond(HttpStatusCode.Created)

            is RegistrationResult.Error -> {
                when (result.type) {
                    RegistrationType.PasswordMatch -> {
                        call.respond(HttpStatusCode.BadRequest, "Passwords do not match")
                    }

                    RegistrationType.UsernameOrEmailTaken -> {
                        call.respond(HttpStatusCode.Conflict, "Username or email already taken")
                    }

                    else -> {
                        call.respond(HttpStatusCode.InternalServerError, "Registration failed")
                    }
                }
            }
        }
    } catch (ex: IllegalStateException) {
        call.respond(HttpStatusCode.BadRequest)
    } catch (ex: JsonConvertException) {
        call.respond(HttpStatusCode.BadRequest)
    }
}
