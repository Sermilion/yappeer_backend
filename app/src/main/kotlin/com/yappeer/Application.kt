package com.yappeer

import com.yappeer.di.configureDi
import com.yappeer.data.plugins.configureDatabases
import com.yappeer.presentation.plugins.configureMonitoring
import com.yappeer.presentation.plugins.configureRateLimiting
import com.yappeer.presentation.plugins.configureRouting
import com.yappeer.presentation.plugins.configureSecurityHeaders
import com.yappeer.presentation.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureDi()
    configureSerialization()
    configureMonitoring()
    configureRateLimiting()
    configureSecurityHeaders()
    configureRouting()
    configureDatabases()
}
