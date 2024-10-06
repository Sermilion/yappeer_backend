import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.20"
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
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

val spotlessConfiguration = {
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("${layout.buildDirectory}/**/*.kt")
            targetExclude("bin/**/*.kt")
            ktlint().editorConfigOverride(
                mapOf(
                    "ij_kotlin_allow_trailing_comma" to true,
                    "ij_kotlin_allow_trailing_comma_on_call_site" to true,
                ),
            )
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
        }
    }

    dependencyLocking {
        // For now lets disable explicit locking
        // lockAllConfigurations()
        unlockAllConfigurations()
    }
}

spotless {
    predeclareDeps()
    spotlessConfiguration()
}

configure<com.diffplug.gradle.spotless.SpotlessExtensionPredeclare> {
    kotlin {
        ktlint()
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    spotlessConfiguration()
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
    add("detektPlugins", "com.twitter.compose.rules:detekt:0.0.26")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("de.mkammerer:argon2-jvm:2.11")
    implementation("io.ktor:ktor-server-auth:3.0.0-rc-2")
    implementation("io.ktor:ktor-server-auth-jwt:3.0.0-rc-2")
    implementation("org.jetbrains.exposed:exposed-java-time:0.55.0")

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

configure<DetektExtension> {
    // Check for module specific detekt config
    val moduleConfig = file("$projectDir/detekt.yml")
    // If the module defines a specific detekt config, we attach it to the main configuration
    if (moduleConfig.exists()) {
        config.setFrom("$rootDir/config/detekt/detekt.yml", moduleConfig)
    }
    baseline = file("$rootDir/config/detekt_baseline.xml")
    basePath = rootDir.absolutePath
    buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = JavaVersion.VERSION_11.toString()
    reports {
        // observe findings in your browser with structure and code snippets
        html.required.set(true)
        // checkstyle like format. This is utilized by the merge reports task
        xml.required.set(true)
    }
}
