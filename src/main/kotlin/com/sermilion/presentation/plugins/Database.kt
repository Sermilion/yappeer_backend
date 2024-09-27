package com.sermilion.presentation.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import java.sql.Connection
import java.sql.DriverManager
import org.h2.util.SortedProperties.loadProperties

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
