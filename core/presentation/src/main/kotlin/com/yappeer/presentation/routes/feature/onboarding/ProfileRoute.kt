package com.yappeer.presentation.routes.feature.onboarding

import com.yappeer.domain.onboarding.model.value.Username
import com.yappeer.domain.onboarding.model.value.ValueValidationException
import com.yappeer.domain.onboarding.repository.OnboardingRepository
import com.yappeer.presentation.routes.model.mapper.ResponseMapper.toUiModel
import com.yappeer.presentation.routes.model.param.ProfileParams
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

internal const val ProfileRoute = "/profile"

suspend fun Route.profileRoute(call: RoutingCall) {
    val onboardingRepository: OnboardingRepository by inject()

    val logger = LoggerFactory.getLogger(ProfileRoute)
    val params = call.receive<ProfileParams>()
    try {
        val result = onboardingRepository.findUser(Username(params.username))

        if (result != null) {
            val profile = result.toUiModel()
            call.respond(HttpStatusCode.OK, profile)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    } catch (e: ValueValidationException) {
        val message = "Validation error for value type ${e.valueType}"
        logger.error(message, e)
        call.respond(HttpStatusCode.BadRequest, message)
    }
}
