package com.yappeer.presentation.routes.feature.onboarding

import com.yappeer.domain.onboarding.model.UserWithPassword
import com.yappeer.domain.onboarding.model.value.Email
import com.yappeer.domain.onboarding.model.value.Password
import com.yappeer.domain.onboarding.repository.OnboardingRepository
import com.yappeer.domain.onboarding.security.UserAuthenticationService
import com.yappeer.presentation.plugins.configureErrorHandling
import com.yappeer.presentation.routes.feature.posts.ERROR_CODE_UNAUTHORIZED
import com.yappeer.presentation.routes.model.param.LoginParams
import com.yappeer.presentation.routes.model.result.ErrorResponse
import com.yappeer.presentation.routes.model.ui.TokenUiModel
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.util.UUID
import kotlin.test.assertEquals

class LoginRouteTest : KoinTest {
    // Mock objects
    private val onboardingRepository = mockk<OnboardingRepository>(relaxed = true)
    private val userAuthenticationService = mockk<UserAuthenticationService>(relaxed = true)

    private val testUserId = UUID.randomUUID()
    private val testEmail = Email("test@example.com")
    private val testPassword = Password("Password123!")
    private val testHashedPassword = Password("HashedPassword123!")
    private val notAnEmail = "not-an-email"

    @Before
    fun setup() {
        stopKoin()

        startKoin {
            modules(
                module {
                    single { onboardingRepository }
                    single { userAuthenticationService }
                },
            )
        }

        // Reset mocks before each test
        clearAllMocks()
    }

    @Test
    fun `test successful login returns tokens`() = testApplication {
        // Setup mocks
        val userWithPassword = UserWithPassword(
            id = testUserId,
            email = testEmail.value,
            username = "testuser",
            password = testHashedPassword.value,
        )

        every { onboardingRepository.findUserWithPassword(testEmail) } returns userWithPassword
        every { userAuthenticationService.verifyPassword(testPassword, testHashedPassword) } returns true
        every { userAuthenticationService.generateAccessToken(testUserId) } returns "access_token"
        every { userAuthenticationService.generateRefreshToken(testUserId) } returns "refresh_token"
        every { userAuthenticationService.getAccessTokenExpiration() } returns 3600L

        // Configure application
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                post(LOGIN_ROUTE) { loginRoute(call) }
            }
        }

        // Create client with JSON support
        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        // Perform login request
        val response = client.post(LOGIN_ROUTE) {
            contentType(ContentType.Application.Json)
            setBody(LoginParams(testEmail.value, testPassword.value))
        }

        // Verify response
        assertEquals(HttpStatusCode.OK, response.status)
        val tokenResponse = response.body<TokenUiModel>()
        assertEquals("access_token", tokenResponse.accessToken)
        assertEquals("refresh_token", tokenResponse.refreshToken)
        assertEquals(3600L, tokenResponse.expiresIn)

        // Verify interactions
        verify { onboardingRepository.findUserWithPassword(testEmail) }
        verify { userAuthenticationService.verifyPassword(testPassword, testHashedPassword) }
        verify { userAuthenticationService.generateAccessToken(testUserId) }
        verify { userAuthenticationService.generateRefreshToken(testUserId) }
        verify { onboardingRepository.updateLastLogin(testUserId, any()) }
    }

    @Test
    fun `test invalid credentials return unauthorized`() = testApplication {
        // Setup mocks
        val userWithPassword = UserWithPassword(
            id = testUserId,
            email = testEmail.value,
            username = "testuser",
            password = testHashedPassword.value,
        )

        every { onboardingRepository.findUserWithPassword(testEmail) } returns userWithPassword
        every { userAuthenticationService.verifyPassword(testPassword, testHashedPassword) } returns false

        // Configure application
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                post(LOGIN_ROUTE) { loginRoute(call) }
            }
        }

        // Create client with JSON support
        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        // Perform login request
        val response = client.post(LOGIN_ROUTE) {
            contentType(ContentType.Application.Json)
            setBody(LoginParams(testEmail.value, testPassword.value))
        }

        // Verify response
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val errorResponse = response.body<ErrorResponse>()
        assertEquals(ERROR_CODE_UNAUTHORIZED, errorResponse.code)

        // Verify interactions
        verify { onboardingRepository.findUserWithPassword(testEmail) }
        verify { userAuthenticationService.verifyPassword(testPassword, testHashedPassword) }
        verify { userAuthenticationService.recordFailedLoginAttempt(testEmail) }
        verify(exactly = 0) { onboardingRepository.updateLastLogin(any(), any()) }
    }

    @Test
    fun `test user not found returns unauthorized`() = testApplication {
        // Setup mocks
        every { onboardingRepository.findUserWithPassword(testEmail) } returns null

        // Configure application
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                post(LOGIN_ROUTE) { loginRoute(call) }
            }
        }

        // Create client with JSON support
        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        // Perform login request
        val response = client.post(LOGIN_ROUTE) {
            contentType(ContentType.Application.Json)
            setBody(LoginParams(testEmail.value, testPassword.value))
        }

        // Verify response
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val errorResponse = response.body<ErrorResponse>()
        assertEquals(ERROR_CODE_UNAUTHORIZED, errorResponse.code)

        // Verify interactions
        verify { onboardingRepository.findUserWithPassword(testEmail) }
        verify { userAuthenticationService.recordFailedLoginAttempt(testEmail) }
    }

    @Test
    fun `test account locked returns too many requests`() = testApplication {
        // Setup mocks
        every { userAuthenticationService.isAccountLocked(testEmail) } returns true

        // Configure application
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                post(LOGIN_ROUTE) { loginRoute(call) }
            }
        }

        // Create client with JSON support
        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        // Perform login request
        val response = client.post(LOGIN_ROUTE) {
            contentType(ContentType.Application.Json)
            setBody(LoginParams(testEmail.value, testPassword.value))
        }

        // Verify response
        assertEquals(HttpStatusCode.TooManyRequests, response.status)
        val errorResponse = response.body<ErrorResponse>()
        assertEquals(ERROR_TYPE_ACCOUNT_LOCKED, errorResponse.code)
    }

    @Test
    fun `test validation failure returns bad request`() = testApplication {
        // Configure application
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            configureErrorHandling()
            routing {
                post(LOGIN_ROUTE) { loginRoute(call) }
            }
        }

        // Create client with JSON support
        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        // Perform login request with invalid email
        val response = client.post(LOGIN_ROUTE) {
            contentType(ContentType.Application.Json)
            setBody(LoginParams(notAnEmail, "pwd"))
        }

        // Verify response
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val errorResponse = response.body<ErrorResponse>()
        assertEquals(ERROR_TYPE_VALIDATION_ERROR, errorResponse.code)
    }
}
