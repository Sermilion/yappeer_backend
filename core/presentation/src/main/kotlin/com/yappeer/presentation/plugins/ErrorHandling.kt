package com.yappeer.presentation.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

fun Application.configureErrorHandling() {
    val logger = LoggerFactory.getLogger("ErrorHandling")
    install(StatusPages) {
        exception<Exception> { call, cause ->
            val message = "App in illegal state as ${cause.message}"
            logger.error(message, cause)
            call.respond(HttpStatusCode.InternalServerError, message)
        }
    }
}
