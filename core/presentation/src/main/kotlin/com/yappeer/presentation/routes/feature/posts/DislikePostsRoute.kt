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

internal const val DISLIKE_POST_ROUTE = "/post/{postId}/dislike"

suspend fun Route.dislikePostRoute(call: RoutingCall) {
  val repository: PostsRepository by inject()

  val logger = LoggerFactory.getLogger(DISLIKE_POST_ROUTE)
  val postId = call.parameters["postId"]?.let { UUID.fromString(it) }
  val userId = call.getCurrentUserId()

  if (postId == null || userId == null) {
    call.respond(HttpStatusCode.BadRequest, "Invalid post ID")
    return
  }

  val result = repository.likePost(
    postId = postId,
    userId = userId,
    status = LikeStatus.Dislike,
  )

  if (result) {
    call.respond(HttpStatusCode.OK)
  } else {
    val message = "Failed to dislike post."
    logger.error(message)
    call.respond(HttpStatusCode.InternalServerError, message)
  }
}
