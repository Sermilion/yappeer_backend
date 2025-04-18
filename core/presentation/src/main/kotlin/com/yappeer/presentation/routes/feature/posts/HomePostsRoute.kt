package com.yappeer.presentation.routes.feature.posts

import com.yappeer.domain.posts.repository.PostsRepository
import com.yappeer.presentation.routes.model.mapper.PostMapper.toUiModel
import com.yappeer.presentation.routes.model.param.PaginatedRequestParams
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

internal const val HOME_POSTS_ROUTE = "/home"

suspend fun Route.homePostsRoute(call: RoutingCall) {
    val repository: PostsRepository by inject()

    val logger = LoggerFactory.getLogger(HOME_POSTS_ROUTE)
    val params = call.receive<PaginatedRequestParams>()

    val result = repository.homePosts(
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
