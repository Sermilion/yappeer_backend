package com.sermilion

import com.sermilion.di.configureDi
import com.sermilion.presentation.plugins.configureDatabases
import com.sermilion.presentation.plugins.configureMonitoring
import com.sermilion.presentation.plugins.configureRouting
import com.sermilion.presentation.plugins.configureSerialization
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
