package com.yappeer.presentation.plugins

import com.yappeer.presentation.routes.feature.onboarding.LOGIN_ROUTE
import com.yappeer.presentation.routes.model.result.ErrorResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.delay
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RateLimitingTest {

    @Test
    fun `test rate limiting for login route`() = testApplication {
        // Configure application with rate limiting for login
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            install(RateLimiting) {
                rateLimit(
                    path = LOGIN_ROUTE,
                    limit = 3,
                    period = java.time.Duration.ofMinutes(5),
                ) { call ->
                    call.request.origin.remoteHost
                }
            }

            routing {
                post(LOGIN_ROUTE) {
                    call.respond(HttpStatusCode.OK, "Login simulation")
                }
            }
        }

        // Create client with JSON support
        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        // First requests should succeed
        repeat(3) {
            val response = client.post(LOGIN_ROUTE)
            assertEquals(HttpStatusCode.OK, response.status)

            // Check rate limit headers
            assertNotNull(response.headers["X-RateLimit-Limit"])
            assertNotNull(response.headers["X-RateLimit-Remaining"])
            assertNotNull(response.headers["X-RateLimit-Reset"])

            // Ensure remaining count is decreasing
            val remaining = response.headers["X-RateLimit-Remaining"]?.toInt() ?: -1
            assertEquals(2 - it, remaining)
        }

        // The next request should be rate limited
        val response = client.post(LOGIN_ROUTE)
        assertEquals(HttpStatusCode.TooManyRequests, response.status)

        // Verify error response structure
        val errorResponse = response.body<ErrorResponse>()
        assertEquals("RATE_LIMIT_EXCEEDED", errorResponse.code)

        // Verify rate limit headers on error response
        assertNotNull(response.headers["X-RateLimit-Limit"])
        assertEquals("0", response.headers["X-RateLimit-Remaining"])
        assertNotNull(response.headers["X-RateLimit-Reset"])
        assertNotNull(response.headers["Retry-After"])
    }

    @Test
    fun `test rate limiting for different paths`() = testApplication {
        // Configure application with rate limiting for different paths
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()

            install(RateLimiting) {
                rateLimit(
                    path = "/api/path1",
                    limit = 2,
                    period = java.time.Duration.ofSeconds(5),
                ) { call ->
                    call.request.origin.remoteHost
                }

                rateLimit(
                    path = "/api/path2",
                    limit = 5,
                    period = java.time.Duration.ofSeconds(5),
                ) { call ->
                    call.request.origin.remoteHost
                }
            }

            routing {
                get("/api/path1") {
                    call.respond("Path 1 response")
                }
                get("/api/path2") {
                    call.respond("Path 2 response")
                }
            }
        }

        // Create client
        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        // Test first path with lower limit
        repeat(2) {
            val response = client.get("/api/path1")
            assertEquals(HttpStatusCode.OK, response.status)
        }

        // Should be rate limited now for path1
        val responsePath1 = client.get("/api/path1")
        assertEquals(HttpStatusCode.TooManyRequests, responsePath1.status)

        // But path2 should still work because it has different limits
        repeat(5) {
            val response = client.get("/api/path2")
            assertEquals(HttpStatusCode.OK, response.status)
        }

        // Now path2 should be rate limited
        val responsePath2 = client.get("/api/path2")
        assertEquals(HttpStatusCode.TooManyRequests, responsePath2.status)
    }

    @Test
    fun `test rate limit reset after period expires`() = testApplication {
        // Configure application with rate limiting with very short period
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()

            install(RateLimiting) {
                rateLimit(
                    path = "/api/short-period",
                    limit = 2,
                    period = java.time.Duration.ofSeconds(1),
                ) { call ->
                    call.request.origin.remoteHost
                }
            }

            routing {
                get("/api/short-period") {
                    call.respond("Short period response")
                }
            }
        }

        // Create client
        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        // Use up the rate limit
        repeat(2) {
            val response = client.get("/api/short-period")
            assertEquals(HttpStatusCode.OK, response.status)
        }

        // Verify we're rate limited
        val rateLimitedResponse = client.get("/api/short-period")
        assertEquals(HttpStatusCode.TooManyRequests, rateLimitedResponse.status)

        // Wait for the rate limit period to expire
        delay(1500) // Wait 1.5 seconds (longer than the 1-second period)

        // Now we should be able to make requests again
        val responseAfterWait = client.get("/api/short-period")
        assertEquals(HttpStatusCode.OK, responseAfterWait.status)
    }
}
