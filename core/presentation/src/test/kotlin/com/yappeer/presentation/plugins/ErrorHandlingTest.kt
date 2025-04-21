package com.yappeer.presentation.plugins

import com.yappeer.presentation.routes.model.result.ErrorResponse
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErrorHandlingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test 404 error handling`() = testApplication {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                // No routes defined, so all requests will 404
            }
        }

        val response = client.get("/non-existent-path")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("NOT_FOUND", errorResponse.code)
        assertEquals("The requested resource could not be found", errorResponse.message)
    }

    @Test
    fun `test method not allowed error handling`() = testApplication {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                get("/test-route") {
                    call.respond("OK")
                }
                // Only GET is defined, POST will trigger method not allowed
            }
        }

        val response = client.post("/test-route")
        assertEquals(HttpStatusCode.MethodNotAllowed, response.status)

        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("METHOD_NOT_ALLOWED", errorResponse.code)
    }

    @Test
    fun `test bad request error handling`() = testApplication {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                get("/bad-request") {
                    throw BadRequestException("Test bad request")
                }
            }
        }

        val response = client.get("/bad-request")
        assertEquals(HttpStatusCode.BadRequest, response.status)

        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("BAD_REQUEST", errorResponse.code)
        assertEquals("Invalid request", errorResponse.message)
        assertTrue(errorResponse.details?.any { it.detail.contains("Test bad request") } ?: false)
    }

    @Test
    fun `test not found exception handling`() = testApplication {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                get("/throw-not-found") {
                    throw NotFoundException()
                }
            }
        }

        val response = client.get("/throw-not-found")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("NOT_FOUND", errorResponse.code)
        assertEquals("Resource not found", errorResponse.message)
    }

    @Test
    fun `test timeout exception handling`() = testApplication {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                get("/timeout") {
                    throw java.util.concurrent.TimeoutException("Operation timed out")
                }
            }
        }

        val response = client.get("/timeout")
        assertEquals(HttpStatusCode.GatewayTimeout, response.status)

        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("TIMEOUT", errorResponse.code)
        assertEquals("Request timed out", errorResponse.message)
        assertTrue(errorResponse.details?.any { it.detail.contains("Operation timed out") } ?: false)
    }

    @Test
    fun `test generic exception handling`() = testApplication {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                get("/generic-error") {
                    throw IllegalStateException("Something went wrong")
                }
            }
        }

        val response = client.get("/generic-error")
        assertEquals(HttpStatusCode.InternalServerError, response.status)

        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("INTERNAL_SERVER_ERROR", errorResponse.code)
        assertEquals("An unexpected error occurred", errorResponse.message)
    }
}
