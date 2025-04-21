package com.yappeer.presentation.plugins

import com.yappeer.domain.onboarding.model.value.ValueValidationException
import com.yappeer.presentation.routes.model.result.ErrorDetail
import com.yappeer.presentation.routes.model.result.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeoutException

private const val ERROR_TYPE_VALIDATION = "ValidationError"

/**
 * Configuration for global error handling.
 * Standardizes the error responses across the application.
 */
fun Application.configureErrorHandling() {
    val logger = LoggerFactory.getLogger("ErrorHandling")
    install(StatusPages) {
        handleValidationExceptions(logger)

        handleBadRequestException()

        handleNotFoundException()

        handleTimeoutException()

        handleOtherExceptions(logger)

        handleNotFoundStatusCode()

        handleMethodNotAllowedStatusCode()

        handleUnauthorizedStatusCode()
    }

    // Additional intercept to log all errors
    @Suppress("TooGenericExceptionCaught")
    intercept(ApplicationCallPipeline.Monitoring) {
        try {
            proceed()
        } catch (e: Throwable) {
            logger.error("Request error: ${context.request.uri}", e)
            throw e // Re-throw to be handled by StatusPages
        }
    }
}

private fun StatusPagesConfig.handleOtherExceptions(logger: Logger) {
    exception<Throwable> { call, cause ->
        logger.error("Unhandled exception", cause)

        call.respond(
            HttpStatusCode.InternalServerError,
            ErrorResponse(
                code = "INTERNAL_SERVER_ERROR",
                message = "An unexpected error occurred",
                // detect dev mode
                details = if (false) {
                    // Include more details in development mode
                    listOf(ErrorDetail("error", cause.message ?: "Unknown error"))
                } else {
                    emptyList()
                },
            ),
        )
    }
}

private fun StatusPagesConfig.handleUnauthorizedStatusCode() {
    status(HttpStatusCode.Unauthorized) { call, _ ->
        call.respond(
            HttpStatusCode.Unauthorized,
            ErrorResponse(
                code = "UNAUTHORIZED",
                message = "Authentication required to access this resource",
            ),
        )
    }
}

private fun StatusPagesConfig.handleMethodNotAllowedStatusCode() {
    status(HttpStatusCode.MethodNotAllowed) { call, _ ->
        call.respond(
            HttpStatusCode.MethodNotAllowed,
            ErrorResponse(
                code = "METHOD_NOT_ALLOWED",
                message = "The HTTP method is not allowed for this endpoint",
            ),
        )
    }
}

private fun StatusPagesConfig.handleNotFoundStatusCode() {
    status(HttpStatusCode.NotFound) { call, _ ->
        call.respond(
            HttpStatusCode.NotFound,
            ErrorResponse(
                code = "NOT_FOUND",
                message = "The requested resource could not be found",
            ),
        )
    }
}

private fun StatusPagesConfig.handleTimeoutException() {
    exception<TimeoutException> { call, cause ->
        call.respond(
            HttpStatusCode.GatewayTimeout,
            ErrorResponse(
                code = "TIMEOUT",
                message = "Request timed out",
                details = listOf(
                    ErrorDetail("timeout", cause.message ?: "The operation took too long to complete"),
                ),
            ),
        )
    }
}

private fun StatusPagesConfig.handleNotFoundException() {
    exception<NotFoundException> { call, cause ->
        call.respond(
            HttpStatusCode.NotFound,
            ErrorResponse(
                code = "NOT_FOUND",
                message = "Resource not found",
                details = listOf(
                    ErrorDetail(
                        field = "resource",
                        detail = cause.message ?: "The requested resource could not be found",
                    ),
                ),
            ),
        )
    }
}

private fun StatusPagesConfig.handleBadRequestException() {
    exception<BadRequestException> { call, cause ->
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(
                code = "BAD_REQUEST",
                message = "Invalid request",
                details = listOf(
                    ErrorDetail(
                        field = "request",
                        detail = cause.message ?: "The request was malformed or contains invalid parameters",
                    ),
                ),
            ),
        )
    }
}

private fun StatusPagesConfig.handleValidationExceptions(logger: Logger) {
    exception<ValueValidationException> { call, cause ->
        val response = ErrorResponse(
            code = ERROR_TYPE_VALIDATION,
            details = listOf(
                ErrorDetail(
                    field = cause.value.name,
                    detail = cause.value.errorMessage,
                ),
            ),
        )
        call.respond(
            HttpStatusCode.BadRequest,
            message = response,
        )
        logger.error(ERROR_TYPE_VALIDATION, cause)
    }
}
