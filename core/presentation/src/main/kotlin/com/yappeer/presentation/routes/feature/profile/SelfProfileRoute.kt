package com.yappeer.presentation.routes.feature.profile

import com.yappeer.domain.onboarding.model.value.ValueValidationException
import com.yappeer.domain.onboarding.repository.OnboardingRepository
import com.yappeer.presentation.common.getCurrentUserId
import com.yappeer.presentation.routes.model.mapper.UserResponseMapper.toSelfUserUiModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

internal const val SELF_PROFILE_ROUTE = "/self_profile"

suspend fun Route.selfProfileRoute(call: RoutingCall) {
    val onboardingRepository: OnboardingRepository by inject()

    val logger = LoggerFactory.getLogger(SELF_PROFILE_ROUTE)

    val principal = call.principal<JWTPrincipal>()
    val userId = principal?.getCurrentUserId()

    try {
        if (userId != null) {
            val result = onboardingRepository.findUser(userId)

            if (result != null) {
                val profile = result.toSelfUserUiModel()
                call.respond(HttpStatusCode.OK, profile)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } else {
            val message = "Invalid token"
            logger.info(message)
            call.respond(HttpStatusCode.Unauthorized, message)
        }
    } catch (e: ValueValidationException) {
        val message = "Validation error for value type ${e.valueType}"
        logger.error(message, e)
        call.respond(HttpStatusCode.BadRequest, message)
    }
}
