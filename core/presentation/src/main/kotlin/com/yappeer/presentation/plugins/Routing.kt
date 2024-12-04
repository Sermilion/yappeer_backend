package com.yappeer.presentation.plugins

import com.yappeer.presentation.routes.feature.onboarding.LOGIN_ROUTE
import com.yappeer.presentation.routes.feature.onboarding.REFRESH_TOKEN_ROUTE
import com.yappeer.presentation.routes.feature.onboarding.REGISTRATION_ROUTE
import com.yappeer.presentation.routes.feature.onboarding.loginRoute
import com.yappeer.presentation.routes.feature.onboarding.refreshTokenRoute
import com.yappeer.presentation.routes.feature.onboarding.registrationRoute
import com.yappeer.presentation.routes.feature.posts.CREATE_POST_ROUTE
import com.yappeer.presentation.routes.feature.posts.HOME_POSTS_ROUTE
import com.yappeer.presentation.routes.feature.posts.USER_POSTS_ROUTE
import com.yappeer.presentation.routes.feature.posts.createPostRoute
import com.yappeer.presentation.routes.feature.posts.homePostsRoute
import com.yappeer.presentation.routes.feature.posts.userPostsRoute
import com.yappeer.presentation.routes.feature.profile.SELF_PROFILE_ROUTE
import com.yappeer.presentation.routes.feature.profile.USER_PROFILE_ROUTE
import com.yappeer.presentation.routes.feature.profile.selfProfileRoute
import com.yappeer.presentation.routes.feature.profile.userProfileRoute
import com.yappeer.presentation.routes.feature.subscriptions.COMMUNITIES_ROUTE
import com.yappeer.presentation.routes.feature.subscriptions.FOLLOWERS_ROUTE
import com.yappeer.presentation.routes.feature.subscriptions.FOLLOWING_ROUTE
import com.yappeer.presentation.routes.feature.subscriptions.TAGS_ROUTE
import com.yappeer.presentation.routes.feature.subscriptions.communitiesRoute
import com.yappeer.presentation.routes.feature.subscriptions.followersRoute
import com.yappeer.presentation.routes.feature.subscriptions.followingRoute
import com.yappeer.presentation.routes.feature.subscriptions.tagsRoute
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
        post(LOGIN_ROUTE) { loginRoute(call) }
        post(REGISTRATION_ROUTE) { registrationRoute(call) }

        authenticate(AUTHENTICATION_IDENTIFIER) {
            get(REFRESH_TOKEN_ROUTE) { refreshTokenRoute(call) }

            get(USER_PROFILE_ROUTE) { userProfileRoute(call) }
            get(SELF_PROFILE_ROUTE) { selfProfileRoute(call) }

            post(FOLLOWERS_ROUTE) { followersRoute(call) }
            post(FOLLOWING_ROUTE) { followingRoute(call) }
            post(TAGS_ROUTE) { tagsRoute(call) }
            post(COMMUNITIES_ROUTE) { communitiesRoute(call) }
            post(USER_POSTS_ROUTE) { userPostsRoute(call) }

            post(CREATE_POST_ROUTE) { createPostRoute(call) }
            post(HOME_POSTS_ROUTE) { homePostsRoute(call) }
            // like post
            // dislike post
            // read post
            // add comment
            // load comments
            // home posts
            // follow user
            // unfollow user
            // follow tag
            // unfollow tag
            // follow community
            // unfollow community
        }
    }
}
