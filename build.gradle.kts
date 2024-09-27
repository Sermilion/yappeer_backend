import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.20"
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ktor)
}

group = "com.sermilion"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.jetbrains.exposed.dao)
    implementation(libs.jetbrains.exposed.jdbc)
    implementation(libs.jetbrains.kotlinx.serialization)
    implementation(libs.jsonpath)
    implementation(libs.h2)
    implementation(libs.ktor.server.statuspage)
    implementation(libs.ktor.server.calllogging)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.contentnegotiation)
    implementation(libs.ktor.server.hostcommon)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.serialization)
    implementation(libs.postgresql)
    implementation(libs.logback)
    implementation(libs.jetbrains.kotlinx.datetime)
    implementation(libs.koin.ktor)
    implementation(libs.commons.codec)

    testImplementation(libs.jetbrains.kotlin.junit)
    testImplementation(libs.ktor.client.contentnegotiation)
    testImplementation(libs.ktor.server.testhost)
    testImplementation(libs.ktor.server.testhotsjvm)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters") // ensure parameter names are preserved for reflection
}
