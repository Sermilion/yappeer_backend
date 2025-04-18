package com.yappeer.presentation.routes.feature.subscriptions

import com.yappeer.domain.subscriptions.repository.SubscriptionsRepository
import com.yappeer.presentation.routes.model.mapper.CommunitiesMapper.toUiModel
import com.yappeer.presentation.routes.model.param.UserSubscriptionsParams
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import java.util.UUID

internal const val COMMUNITIES_ROUTE = "/communities"

suspend fun Route.communitiesRoute(call: RoutingCall) {
    val repository: SubscriptionsRepository by inject()

    val request = call.receive<UserSubscriptionsParams>()

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
}
