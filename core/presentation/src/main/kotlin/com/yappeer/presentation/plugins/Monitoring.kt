package com.yappeer.presentation.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.path

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = org.slf4j.event.Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}
