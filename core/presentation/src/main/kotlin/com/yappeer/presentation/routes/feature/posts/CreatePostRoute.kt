package com.yappeer.presentation.routes.feature.posts

import com.yappeer.domain.posts.repository.PostsRepository
import com.yappeer.presentation.common.getCurrentUserId
import com.yappeer.presentation.routes.model.mapper.PostMapper.toUiModel
import com.yappeer.presentation.routes.model.param.CreatePostParams
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

internal const val CREATE_POST_ROUTE = "/create_post"

suspend fun Route.createPostRoute(call: RoutingCall) {
    val repository: PostsRepository by inject()

    val params = call.receive<CreatePostParams>()
    val userId = call.getCurrentUserId()

    if (userId == null) {
        val message = "Invalid token"
        call.respond(HttpStatusCode.Unauthorized, message)
        return
    }

    val logger = LoggerFactory.getLogger(CREATE_POST_ROUTE)

    val post = repository.createPost(
        title = params.title,
        content = params.content,
        tags = params.tags,
        createdBy = userId,
    )

    if (post != null) {
        call.respond(HttpStatusCode.Created, post.toUiModel())
    } else {
        logger.error("Failed to create post.")
        call.respond(HttpStatusCode.InternalServerError)
    }
}
