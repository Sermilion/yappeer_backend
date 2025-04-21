package com.yappeer.presentation.plugins

import com.yappeer.presentation.routes.model.result.ErrorResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.Test
import java.util.Base64
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for the error handling functionality, testing interaction with actual routes
 * and real-world scenarios.
 */
class ErrorHandlingIntegrationTest {

    @Test
    fun `test authentication handling covers both failure and success scenarios`() = testApplication {
        application {
            install(Authentication) {
                basic("auth-basic") {
                    validate { credentials ->
                        if (credentials.name == "test" && credentials.password == "password") {
                            UserIdPrincipal(credentials.name)
                        } else {
                            null
                        }
                    }
                }
            }
            install(ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                authenticate("auth-basic") {
                    get("/protected") {
                        call.respond(
                            HttpStatusCode.OK,
                            ErrorResponse(code = "ContentProtectedError", message = "Protected content"),
                        )
                    }
                }
            }
        }

        // Test 1: Missing authentication
        val unauthorizedResponse = client.get("/protected")
        assertEquals(HttpStatusCode.Unauthorized, unauthorizedResponse.status)

        val errorResponse = Json.decodeFromString<ErrorResponse>(unauthorizedResponse.bodyAsText())
        assertEquals("UNAUTHORIZED", errorResponse.code)

        // Test 2: Correct authentication
        val authorizedResponse = client.get("/protected") {
            header(
                key = HttpHeaders.Authorization,
                value = "Basic ${Base64.getEncoder().encodeToString("test:password".toByteArray())}",
            )
        }
        assertEquals(HttpStatusCode.OK, authorizedResponse.status)

        val successResponse = Json.decodeFromString<ErrorResponse>(authorizedResponse.bodyAsText())
        assertEquals("ContentProtectedError", successResponse.code)
        assertEquals("Protected content", successResponse.message)
    }

    @Suppress("UseCheckOrError")
    @Test
    fun `test error propagation from application logic`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                get("/business-logic-error") {
                    // Simulate a business logic error that occurs during processing
                    if (call.request.queryParameters["trigger"] == "error") {
                        throw IllegalStateException("Business logic failed")
                    }
                    call.respond("Success")
                }
            }
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        val response = client.get("/business-logic-error?trigger=error")
        assertEquals(HttpStatusCode.InternalServerError, response.status)

        val errorResponse = response.body<ErrorResponse>()
        assertEquals("INTERNAL_SERVER_ERROR", errorResponse.code)
    }

    @Test
    fun `test path with invalid parameter type`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                get("/items/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid ID format")
                    call.respond("Item $id")
                }
            }
        }

        val response = client.get("/items/not-a-number")
        assertEquals(HttpStatusCode.BadRequest, response.status)

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("BAD_REQUEST", errorResponse.code)
        assertTrue {
            errorResponse.details.any {
                it.detail.contains("Invalid ID format")
            }
        }
    }

    @Test
    fun `test rate limiting error handling`() = testApplication {
        // This test simulates a rate limiting error response
        application {
            install(ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                get("/simulated-rate-limit") {
                    call.response.status(HttpStatusCode.TooManyRequests)
                    call.respond(
                        HttpStatusCode.TooManyRequests,
                        ErrorResponse(
                            code = "RATE_LIMIT_EXCEEDED",
                            message = "Too many requests",
                        ),
                    )
                }
            }
        }

        val response = client.get("/simulated-rate-limit")
        assertEquals(HttpStatusCode.TooManyRequests, response.status)

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("RATE_LIMIT_EXCEEDED", errorResponse.code)
    }
}
