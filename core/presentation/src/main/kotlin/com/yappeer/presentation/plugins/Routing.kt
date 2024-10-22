package com.yappeer.presentation.plugins

import com.yappeer.presentation.routes.feature.onboarding.LoginRoute
import com.yappeer.presentation.routes.feature.onboarding.RefreshTokenRoute
import com.yappeer.presentation.routes.feature.onboarding.RegistrationRoute
import com.yappeer.presentation.routes.feature.onboarding.loginRoute
import com.yappeer.presentation.routes.feature.onboarding.refreshTokenRoute
import com.yappeer.presentation.routes.feature.onboarding.registrationRoute
import com.yappeer.presentation.routes.feature.profile.FollowersRoute
import com.yappeer.presentation.routes.feature.profile.FollowingRoute
import com.yappeer.presentation.routes.feature.profile.SelfProfileRoute
import com.yappeer.presentation.routes.feature.profile.TagsRoute
import com.yappeer.presentation.routes.feature.profile.UserProfileRoute
import com.yappeer.presentation.routes.feature.profile.followersRoute
import com.yappeer.presentation.routes.feature.profile.followingRoute
import com.yappeer.presentation.routes.feature.profile.selfProfileRoute
import com.yappeer.presentation.routes.feature.profile.tagsRoute
import com.yappeer.presentation.routes.feature.profile.userProfileRoute
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.resources.Resources
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    install(Resources)
    configureAuthentication()
    configureErrorHandling()

    routing {
        openAPI(path = "openapi", swaggerFile = "config/openapi/documentation.yaml")
        post(LoginRoute) { loginRoute(call) }
        post(RegistrationRoute) { registrationRoute(call) }

        authenticate(AuthenticationIdentifier) {
            get(UserProfileRoute) { userProfileRoute(call) }
            get(SelfProfileRoute) { selfProfileRoute(call) }
            get(RefreshTokenRoute) { refreshTokenRoute(call) }
            post(FollowersRoute) { followersRoute(call) }
            post(FollowingRoute) { followingRoute(call) }
            post(TagsRoute) { tagsRoute(call) }
        }
    }
}
