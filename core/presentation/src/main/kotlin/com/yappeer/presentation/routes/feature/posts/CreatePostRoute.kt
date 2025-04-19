package com.yappeer.presentation.routes.feature.posts

import com.yappeer.domain.posts.repository.PostsRepository
import com.yappeer.presentation.common.getCurrentUserId
import com.yappeer.presentation.routes.model.mapper.PostMapper.toUiModel
import com.yappeer.presentation.routes.model.param.CreatePostParams
import com.yappeer.presentation.routes.model.result.ErrorDetail
import com.yappeer.presentation.routes.model.result.ErrorResponse
import com.yappeer.presentation.routes.model.ui.PostUiModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.util.UUID

const val CREATE_POST_ROUTE = "/create_post"

// Validation constants
private const val MAX_TITLE_LENGTH = 200
private const val MIN_TITLE_LENGTH = 1
private const val MAX_CONTENT_LENGTH = 5000
private const val MIN_CONTENT_LENGTH = 1
private const val MAX_TAGS_COUNT = 10
private const val MAX_TAG_LENGTH = 30
private const val MAX_COMMUNITIES_COUNT = 5
private const val MAX_MEDIA_URLS_COUNT = 10
private const val MAX_MEDIA_URL_LENGTH = 1024

const val ERROR_CODE_VALIDATION = "VALIDATION_ERROR"
const val ERROR_CODE_UNAUTHORIZED = "UNAUTHORIZED"
const val ERROR_CODE_POST_CREATION_FAILED = "POST_CREATION_FAILED"

private val logger = LoggerFactory.getLogger(CREATE_POST_ROUTE)

suspend fun Route.createPostRoute(call: RoutingCall) {
    val repository: PostsRepository by inject()
    val params = call.receive<CreatePostParams>()

    // Validate the input parameters
    val validationErrors = validateCreatePostParams(params)
    if (validationErrors.isNotEmpty()) {
        logger.warn("Post creation validation failed: ${validationErrors.joinToString { it.field }}")
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(
                code = ERROR_CODE_VALIDATION,
                details = validationErrors,
                message = "Invalid post parameters",
            ),
        )
        return
    }

    val userId = call.getCurrentUserId()

    if (userId == null) {
        logger.warn("Unauthorized attempt to create post")
        call.respond(
            HttpStatusCode.Unauthorized,
            ErrorResponse(
                code = ERROR_CODE_UNAUTHORIZED,
                message = "Invalid token",
            ),
        )
        return
    }

    val communityUuids = params.communityIds.mapNotNull { communityIdStr ->
        try {
            UUID.fromString(communityIdStr)
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid community ID format: $communityIdStr", e)
            null
        }
    }

    // Create metrics for monitoring
    val startTime = System.currentTimeMillis()

    val post = repository.createPost(
        title = params.title,
        content = params.content,
        tags = params.tags,
        communityIds = communityUuids,
        mediaUrls = params.mediaUrls,
        createdBy = userId,
    )

    // Record operation time for monitoring
    val operationTime = System.currentTimeMillis() - startTime
    logger.info("Post creation took $operationTime ms")

    if (post != null) {
        logger.info(
            "Post created successfully with ID ${post.id} by user $userId with ${params.tags.size} tags, " +
                "${communityUuids.size} communities, and ${params.mediaUrls.size} media URLs",
        )

        // Return enhanced response with post data
        call.respond(
            HttpStatusCode.Created,
            CreatePostResponse(
                post = post.toUiModel(),
                status = "success",
                message = "Post created successfully",
            ),
        )
    } else {
        logger.error("Failed to create post. Title: ${params.title}, User: $userId, Tags: ${params.tags.size}")
        call.respond(
            HttpStatusCode.InternalServerError,
            ErrorResponse(
                code = ERROR_CODE_POST_CREATION_FAILED,
                message = "Failed to create your post. Please try again later.",
            ),
        )
    }
}

@Suppress("CyclomaticComplexMethod")
fun validateCreatePostParams(params: CreatePostParams): List<ErrorDetail> {
    val errors = mutableListOf<ErrorDetail>()

    // Validate title
    if (params.title.isBlank()) {
        errors.add(ErrorDetail("title", "Title cannot be empty"))
    } else if (params.title.length < MIN_TITLE_LENGTH) {
        errors.add(ErrorDetail("title", "Title must be at least $MIN_TITLE_LENGTH characters"))
    } else if (params.title.length > MAX_TITLE_LENGTH) {
        errors.add(ErrorDetail("title", "Title cannot exceed $MAX_TITLE_LENGTH characters"))
    }

    // Validate content
    if (params.content.isBlank()) {
        errors.add(ErrorDetail("content", "Content cannot be empty"))
    } else if (params.content.length < MIN_CONTENT_LENGTH) {
        errors.add(ErrorDetail("content", "Content must be at least $MIN_CONTENT_LENGTH characters"))
    } else if (params.content.length > MAX_CONTENT_LENGTH) {
        errors.add(ErrorDetail("content", "Content cannot exceed $MAX_CONTENT_LENGTH characters"))
    }

    // Validate tags
    if (params.tags.size > MAX_TAGS_COUNT) {
        errors.add(ErrorDetail("tags", "Maximum of $MAX_TAGS_COUNT tags allowed"))
    }

    // Validate each tag
    params.tags.forEachIndexed { index, tag ->
        if (tag.isBlank()) {
            errors.add(ErrorDetail("tags[$index]", "Tag cannot be empty"))
        } else if (tag.length > MAX_TAG_LENGTH) {
            errors.add(ErrorDetail("tags[$index]", "Tag cannot exceed $MAX_TAG_LENGTH characters"))
        }
    }

    // Validate communities
    if (params.communityIds.size > MAX_COMMUNITIES_COUNT) {
        errors.add(ErrorDetail("communityIds", "Maximum of $MAX_COMMUNITIES_COUNT communities allowed"))
    }

    // Validate each community ID format
    params.communityIds.forEachIndexed { index, communityId ->
        try {
            UUID.fromString(communityId)
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid community ID format: $communityId", e)
            errors.add(ErrorDetail("communityIds[$index]", "Invalid community ID format"))
        }
    }

    // Validate media URLs
    if (params.mediaUrls.size > MAX_MEDIA_URLS_COUNT) {
        errors.add(ErrorDetail("mediaUrls", "Maximum of $MAX_MEDIA_URLS_COUNT media URLs allowed"))
    }

    // Validate each media URL
    params.mediaUrls.forEachIndexed { index, mediaUrl ->
        if (mediaUrl.isBlank()) {
            errors.add(ErrorDetail("mediaUrls[$index]", "Media URL cannot be empty"))
        } else if (mediaUrl.length > MAX_MEDIA_URL_LENGTH) {
            errors.add(
                ErrorDetail(
                    field = "mediaUrls[$index]",
                    detail = "Media URL cannot exceed $MAX_MEDIA_URL_LENGTH characters",
                ),
            )
        } else if (!isValidUrl(mediaUrl)) {
            errors.add(ErrorDetail("mediaUrls[$index]", "Invalid URL format"))
        }
    }

    return errors
}

@Suppress("ReturnCount")
private fun isValidUrl(url: String): Boolean {
    // URL must start with http:// or https://
    if (!(url.startsWith("http://") || url.startsWith("https://"))) {
        return false
    }

    // URL must contain a domain after the protocol
    val protocolSplit = url.split("://")
    if (protocolSplit.size != 2 || protocolSplit[1].isBlank()) {
        return false
    }

    // Basic domain validation
    val domainPart = protocolSplit[1].split("/")[0]
    return !(domainPart.isBlank() || !domainPart.contains("."))
}

// Enhanced response type with status information
@Serializable
data class CreatePostResponse(
    val post: PostUiModel,
    val status: String = "success",
    val message: String = "Post created successfully",
)
