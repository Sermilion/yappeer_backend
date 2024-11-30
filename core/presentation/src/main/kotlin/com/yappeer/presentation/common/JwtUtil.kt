package com.yappeer.presentation.common

import com.yappeer.domain.onboarding.security.UserAuthenticationService.Companion.CLAIM_USER_ID
import io.ktor.server.auth.jwt.JWTPrincipal
import java.util.UUID

fun JWTPrincipal.getCurrentUserId(): UUID? {
    return payload.getClaim(CLAIM_USER_ID)?.asString()?.let {
        UUID.fromString(it)
    }
}
