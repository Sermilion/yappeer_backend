package com.yappeer.presentation.routes.feature.subscriptions

import com.yappeer.domain.onboarding.model.value.ValueValidationException
import com.yappeer.domain.subscriptions.repository.SubscriptionsRepository
import com.yappeer.presentation.routes.model.mapper.CommunitiesMapper.toUiModel
import com.yappeer.presentation.routes.model.mapper.TagsResponseMapper.toUiModel
import com.yappeer.presentation.routes.model.param.UserSubscriptionsParams
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.util.UUID

internal const val COMMUNITIES_ROUTE = "/communities"

suspend fun Route.communitiesRoute(call: RoutingCall) {
    val repository: SubscriptionsRepository by inject()

    val logger = LoggerFactory.getLogger(TAGS_ROUTE)
    val request = call.receive<UserSubscriptionsParams>()

    try {
        val result = repository.findFollowedCommunities(
            userId = UUID.fromString(request.userId),
            page = request.page,
            pageSize = request.pageSize,
        )

        if (result != null) {
            call.respond(HttpStatusCode.OK, result.toUiModel())
        } else {
            call.respond(HttpStatusCode.InternalServerError)
        }
    } catch (e: ValueValidationException) {
        val message = "Validation error for value type ${e.valueType}"
        logger.error(message, e)
        call.respond(HttpStatusCode.BadRequest, message)
    }
}
