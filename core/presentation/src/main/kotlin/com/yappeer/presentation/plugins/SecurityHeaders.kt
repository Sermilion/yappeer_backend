package com.yappeer.presentation.plugins

import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.hsts.HSTS
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.response.ApplicationSendPipeline

/**
 * Configures security headers and other security-related configurations
 */
private const val ONE_YEAR_SECONDS = 31536000L
fun Application.configureSecurityHeaders() {
    // HSTS
    install(HSTS) {
        maxAgeInSeconds = ONE_YEAR_SECONDS
        includeSubDomains = true
        preload = true
    }

    // Content compression
    install(Compression) {
        gzip()
    }

    // Partial content support (for range requests)
    install(PartialContent)

    // Support for forwarded headers
    install(XForwardedHeaders)
    install(ForwardedHeaders)

    // Add security headers to all responses
    sendPipeline.intercept(ApplicationSendPipeline.Before) {
        this.context.response.headers.apply {
            append(FRAME_OPTIONS, "DENY")
            append("Referrer-Policy", "strict-origin-when-cross-origin")
            append(HttpHeaders.CacheControl, "no-store, no-cache, must-revalidate")
            append("Pragma", "no-cache")
        }
    }
}

const val FRAME_OPTIONS = "X-Frame-Options"
