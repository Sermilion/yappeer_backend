package com.yappeer

import com.yappeer.di.configureDi
import com.yappeer.presentation.plugins.configureDatabases
import com.yappeer.presentation.plugins.configureMonitoring
import com.yappeer.presentation.plugins.configureRouting
import com.yappeer.presentation.plugins.configureSerialization
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDi()
    configureSerialization()
    configureMonitoring()
    configureRouting()
    configureDatabases()
}
