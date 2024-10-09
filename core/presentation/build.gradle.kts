plugins {
    alias(libs.plugins.yappeer.ktor.library)
}

group = "com.yappeer.core.presentation"
version = "unspecified"

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.jetbrains.exposed.dao)
    implementation(libs.jetbrains.kotlinx.serialization)
    implementation(libs.jsonpath)
    implementation(libs.h2)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.calllogging)
    implementation(libs.ktor.server.contentnegotiation)
    implementation(libs.ktor.server.hostcommon)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.server.statuspage)
    implementation(libs.jetbrains.kotlinx.datetime)
    implementation(libs.commons.codec)
}