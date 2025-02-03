package com.yappeer.presentation.common

import com.yappeer.domain.onboarding.security.UserAuthenticationService.Companion.CLAIM_USER_ID
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingCall
import java.util.UUID

fun RoutingCall.getCurrentUserId(): UUID? {
    return this.principal<JWTPrincipal>()?.payload?.getClaim(CLAIM_USER_ID)?.asString()?.let {
        UUID.fromString(it)
    }
}
