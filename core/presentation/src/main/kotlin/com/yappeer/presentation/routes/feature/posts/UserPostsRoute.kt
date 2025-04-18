package com.yappeer.presentation.routes.feature.posts

import com.yappeer.domain.posts.repository.PostsRepository
import com.yappeer.presentation.routes.model.mapper.PostMapper.toUiModel
import com.yappeer.presentation.routes.model.param.UserSubscriptionsParams
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.util.UUID

internal const val USER_POSTS_ROUTE = "/user_posts"

suspend fun Route.userPostsRoute(call: RoutingCall) {
    val repository: PostsRepository by inject()

    val params = call.receive<UserSubscriptionsParams>()

    val logger = LoggerFactory.getLogger(USER_POSTS_ROUTE)

    val result = repository.userPosts(
        userId = UUID.fromString(params.userId),
        page = params.page,
        pageSize = params.pageSize,
    )

    if (result != null) {
        call.respond(HttpStatusCode.OK, result.toUiModel())
    } else {
        logger.error("Failed to load posts.")
        call.respond(HttpStatusCode.InternalServerError)
    }
}
