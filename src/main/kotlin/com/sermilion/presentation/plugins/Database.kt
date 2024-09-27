package com.sermilion.presentation.plugins

import io.ktor.server.application.Application
import org.h2.util.SortedProperties.loadProperties
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val properties = loadProperties("local.properties") // Load properties from the file

    val username = properties.getProperty("postgres.username")
    val password = properties.getProperty("postgres.password")
    val url = properties.getProperty("postgres.url")

    Database.connect(
        url = url,
        user = username,
        password = password,
    )
}
