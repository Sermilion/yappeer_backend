package com.yappeer.presentation.routes.feature.profile

import com.yappeer.domain.onboarding.repository.OnboardingRepository
import com.yappeer.presentation.routes.model.mapper.UserResponseMapper.toUiModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.util.UUID

internal const val USER_PROFILE_ROUTE = "/user/profile"
private const val USER_ID_PARAM = "user_id"

suspend fun Route.userProfileRoute(call: RoutingCall) {
    val onboardingRepository: OnboardingRepository by inject()

    val logger = LoggerFactory.getLogger(USER_PROFILE_ROUTE)

    val userId = call.request.queryParameters[USER_ID_PARAM]?.let {
        UUID.fromString(it)
    }

    if (userId != null) {
        val result = onboardingRepository.findUser(userId)

        if (result != null) {
            val profile = result.toUiModel()
            call.respond(HttpStatusCode.OK, profile)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    } else {
        logger.info("User not found.")
        call.respond(HttpStatusCode.NotFound)
    }
}
