package com.yappeer.presentation.routes.feature.posts

import com.yappeer.domain.posts.model.LikeStatus
import com.yappeer.domain.posts.repository.PostsRepository
import com.yappeer.presentation.common.getCurrentUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.util.UUID

internal const val LIKE_POST_ROUTE = "/post/{postId}/like"

suspend fun Route.likePostRoute(call: RoutingCall) {
    val repository: PostsRepository by inject()

    val logger = LoggerFactory.getLogger(LIKE_POST_ROUTE)
    val postId = call.parameters["postId"]?.let { UUID.fromString(it) }
    val userId = call.getCurrentUserId()

    if (postId == null || userId == null) {
        call.respond(HttpStatusCode.BadRequest, "Invalid post ID")
        return
    }

    val result = repository.likePost(
        postId = postId,
        userId = userId,
        status = LikeStatus.Like,
    )

    if (result) {
        call.respond(HttpStatusCode.OK)
    } else {
        logger.error("Failed to like post.")
        call.respond(HttpStatusCode.InternalServerError)
    }
}
