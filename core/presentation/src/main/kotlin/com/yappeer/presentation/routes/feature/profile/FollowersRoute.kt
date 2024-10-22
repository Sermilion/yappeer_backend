package com.yappeer.presentation.routes.feature.profile

import com.yappeer.domain.content.model.FollowersResult
import com.yappeer.domain.content.repository.SubscriptionsRepository
import com.yappeer.domain.onboarding.model.value.ValueValidationException
import com.yappeer.presentation.routes.model.mapper.FollowersResponseMapper.toUiModel
import com.yappeer.presentation.routes.model.param.UserSubscriptionsParams
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.util.UUID

internal const val FOLLOWERS_ROUTE = "/followers"

suspend fun Route.followersRoute(call: RoutingCall) {
    val repository: SubscriptionsRepository by inject()

    val logger = LoggerFactory.getLogger(FOLLOWERS_ROUTE)
    val request = call.receive<UserSubscriptionsParams>()

    try {
        val result = repository.findFollowers(
            userId = UUID.fromString(request.userId),
            page = request.page,
            pageSize = request.pageSize,
        )

        when (result) {
            is FollowersResult.Data -> {
                call.respond(HttpStatusCode.OK, result.toUiModel())
            }
            FollowersResult.Error -> call.respond(HttpStatusCode.InternalServerError)
        }
    } catch (e: ValueValidationException) {
        val message = "Validation error for value type ${e.valueType}"
        logger.error(message, e)
        call.respond(HttpStatusCode.BadRequest, message)
    }
}
