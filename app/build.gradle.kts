plugins {
    kotlin("jvm") version "2.0.20"
    alias(libs.plugins.sermilion.ktor.library)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

group = "com.sermilion"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

kover {
    reports {

        filters {
            includes {
                classes(listOf("com.sermilion.ktor.*"))
            }
            excludes {
                classes(file("kover-excludes.conf").readLines())
            }
        }

        verify {
            rule("Minimal line coverage rate in percent") {
                bound {
                    // TODO: change this to 50 when tests written
                    minValue = 0
                    aggregationForGroup =
                        kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
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
    implementation(libs.jetbrains.kotlinx.datetime)
    implementation(libs.commons.codec)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("de.mkammerer:argon2-jvm:2.1")
    implementation("io.ktor:ktor-server-auth:3.0.0-rc-2")
    implementation("io.ktor:ktor-server-auth-jwt:3.0.0-rc-2")
    implementation("org.jetbrains.exposed:exposed-java-time:0.54.0")

    testImplementation(libs.jetbrains.kotlin.junit)
    testImplementation(libs.ktor.client.contentnegotiation)
    testImplementation(libs.ktor.server.testhost)
    testImplementation(libs.ktor.server.testhotsjvm)
}

tasks.test {
    useJUnitPlatform()
}
