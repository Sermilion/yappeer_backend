plugins {
    kotlin("jvm") version "2.0.20"
    alias(libs.plugins.yappeer.ktor.library)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

group = "com.yappeer"
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
                classes(listOf("com.yappeer.ktor.*"))
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
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:presentation"))

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)

    // temporary, until I figure out how to make tests run in their respective module
    testImplementation(libs.jetbrains.exposed.dao)
    testImplementation(libs.jetbrains.exposed.jdbc)
    testImplementation(libs.jetbrains.exposed.time)
    testImplementation(libs.jetbrains.kotlinx.datetime)
}

tasks.test {
    useJUnitPlatform()
}
