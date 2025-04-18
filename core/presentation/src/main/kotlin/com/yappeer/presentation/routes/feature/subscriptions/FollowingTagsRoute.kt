package com.yappeer.presentation.routes.feature.subscriptions

import com.yappeer.domain.subscriptions.model.TagsResult
import com.yappeer.domain.subscriptions.repository.SubscriptionsRepository
import com.yappeer.presentation.routes.model.mapper.TagsResponseMapper.toUiModel
import com.yappeer.presentation.routes.model.param.UserSubscriptionsParams
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import java.util.UUID

internal const val TAGS_ROUTE = "/tags"

suspend fun Route.tagsRoute(call: RoutingCall) {
    val repository: SubscriptionsRepository by inject()

    val request = call.receive<UserSubscriptionsParams>()

    val result = repository.findFollowedTags(
        userId = UUID.fromString(request.userId),
        page = request.page,
        pageSize = request.pageSize,
    )

    when (result) {
        is TagsResult.Data -> call.respond(HttpStatusCode.OK, result.toUiModel())
        TagsResult.Error -> call.respond(HttpStatusCode.InternalServerError)
    }
}
