package com.yappeer.presentation.routes.feature.profile

import com.yappeer.domain.onboarding.repository.OnboardingRepository
import com.yappeer.presentation.common.getCurrentUserId
import com.yappeer.presentation.routes.model.mapper.UserResponseMapper.toSelfUserUiModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

internal const val SELF_PROFILE_ROUTE = "/me/profile"

suspend fun Route.selfProfileRoute(call: RoutingCall) {
    val onboardingRepository: OnboardingRepository by inject()

    val logger = LoggerFactory.getLogger(SELF_PROFILE_ROUTE)
    val userId = call.getCurrentUserId()

    if (userId != null) {
        val result = onboardingRepository.findUser(userId)

        if (result != null) {
            val profile = result.toSelfUserUiModel()
            call.respond(HttpStatusCode.OK, profile)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    } else {
        logger.info("Invalid token")
        call.respond(HttpStatusCode.Unauthorized)
    }
}
