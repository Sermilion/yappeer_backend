package com.yappeer.presentation.plugins

import com.yappeer.domain.onboarding.model.value.ValueValidationException
import com.yappeer.presentation.routes.model.result.ErrorDetail
import com.yappeer.presentation.routes.model.result.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

private const val ERROR_TYPE_VALIDATION = "ValidationError"
private const val INTERNAL_ERROR = "InternalError"

fun Application.configureErrorHandling() {
    val logger = LoggerFactory.getLogger("ErrorHandling")
    install(StatusPages) {
        exception<Exception> { call, cause ->
            val message = "App in illegal state: ${cause.message}"
            when (cause) {
                is ValueValidationException -> {
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
                }

                else -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(
                            code = INTERNAL_ERROR,
                            details = emptyList(),
                            message = message,
                        ),
                    )
                }
            }
            logger.error(message, cause)
        }
    }
}
