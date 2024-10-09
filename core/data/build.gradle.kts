plugins {
    alias(libs.plugins.yappeer.ktor.library)
}

group = "com.yappeer.core.data"
version = "unspecified"

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.argon2)

    implementation(libs.jetbrains.exposed.dao)
    implementation(libs.jetbrains.exposed.jdbc)
    implementation(libs.jetbrains.exposed.time)

    implementation(libs.jetbrains.kotlinx.datetime)

    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)

    implementation(libs.h2)

    implementation(libs.postgresql)
}
