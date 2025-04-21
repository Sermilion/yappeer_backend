package com.yappeer.presentation.plugins

import com.yappeer.presentation.common.getCurrentUserId
import com.yappeer.presentation.routes.feature.onboarding.LOGIN_ROUTE
import com.yappeer.presentation.routes.feature.posts.CREATE_POST_ROUTE
import com.yappeer.presentation.routes.model.result.ErrorDetail
import com.yappeer.presentation.routes.model.result.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.origin
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.util.AttributeKey
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class RateLimitingConfiguration {
    val limits = mutableMapOf<String, RateLimitConfig>()

    fun rateLimit(path: String, limit: Int, period: Duration, identityResolver: suspend (ApplicationCall) -> String) {
        limits[path] = RateLimitConfig(limit, period, identityResolver)
    }

    data class RateLimitConfig(
        val limit: Int,
        val period: Duration,
        val identityResolver: suspend (ApplicationCall) -> String,
    )
}

class RateLimiting(config: RateLimitingConfiguration) {
    private val limits = config.limits
    private val requestCounts = ConcurrentHashMap<Pair<String, String>, RequestCount>()
    private val mutex = Mutex()

    data class RequestCount(
        var count: Int,
        var resetAt: Instant,
    )

    suspend fun intercept(context: ApplicationCall) {
        val path = context.request.path()

        // Find rate limit config for this path
        val rateLimitConfig = limits[path] ?: return

        // Get identity for this request
        val identity = rateLimitConfig.identityResolver(context)
        val key = Pair(path, identity)

        mutex.withLock {
            val now = Instant.now()

            // Get or create request count for this identity
            val requestCount = requestCounts.computeIfAbsent(key) {
                RequestCount(0, now.plus(rateLimitConfig.period))
            }

            // Reset count if period has expired
            if (now.isAfter(requestCount.resetAt)) {
                requestCount.count = 0
                requestCount.resetAt = now.plus(rateLimitConfig.period)
            }

            // Check if limit exceeded
            if (requestCount.count >= rateLimitConfig.limit) {
                val retryAfterSecs = Duration.between(now, requestCount.resetAt).seconds
                context.response.headers.append("X-RateLimit-Limit", rateLimitConfig.limit.toString())
                context.response.headers.append("X-RateLimit-Remaining", "0")
                context.response.headers.append("X-RateLimit-Reset", requestCount.resetAt.epochSecond.toString())
                context.response.headers.append("Retry-After", retryAfterSecs.toString())

                context.respond(
                    HttpStatusCode.TooManyRequests,
                    ErrorResponse(
                        code = "RATE_LIMIT_EXCEEDED",
                        details = listOf(ErrorDetail("request", "Rate limit exceeded")),
                        message = "Too many requests. Try again in $retryAfterSecs seconds.",
                    ),
                )

                return
            }

            // Increment request count
            requestCount.count++

            // Set rate limit headers
            context.response.headers.append("X-RateLimit-Limit", rateLimitConfig.limit.toString())
            context.response.headers.append(
                name = "X-RateLimit-Remaining",
                value = (rateLimitConfig.limit - requestCount.count).toString(),
            )
            context.response.headers.append("X-RateLimit-Reset", requestCount.resetAt.epochSecond.toString())
        }
    }

    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, RateLimitingConfiguration, RateLimiting> {
        override val key = AttributeKey<RateLimiting>("RateLimiting")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: RateLimitingConfiguration.() -> Unit,
        ): RateLimiting {
            val configuration = RateLimitingConfiguration().apply(configure)
            val plugin = RateLimiting(configuration)

            pipeline.intercept(ApplicationCallPipeline.Call) {
                plugin.intercept(call)
            }

            return plugin
        }
    }
}

/**
 * Gets a more reliable client identifier by considering multiple headers and IP
 */
private fun ApplicationRequest.getClientIdentifier(): String {
    val forwardedFor = this.headers["X-Forwarded-For"]
    val realIp = this.headers["X-Real-IP"]
    return forwardedFor ?: realIp ?: this.origin.remoteHost
}

fun Application.configureRateLimiting() {
    install(RateLimiting) {
        // Rate limit for login attempts - more strict
        rateLimit(
            path = LOGIN_ROUTE,
            limit = 10,
            period = 5.minutes.toJavaDuration(),
        ) { call ->
            // For login, we use IP-based limiting
            call.request.getClientIdentifier()
        }

        // Rate limit for post creation
        rateLimit(
            path = CREATE_POST_ROUTE,
            limit = 5,
            period = Duration.ofMinutes(1),
        ) { call ->
            // For authenticated endpoints, use user ID if available
            call.getCurrentUserId()?.toString() ?: call.request.getClientIdentifier()
        }

        // Add more rate limits for other routes as needed
    }
}
