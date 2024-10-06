plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.sermilion.ktor.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover) apply false
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

// https://issuetracker.google.com/issues/328871352?pli=1
gradle.startParameter.excludedTaskNames.addAll(listOf(":build-logic:convention:testClasses"))

repositories {
    mavenCentral()
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

spotless {
    predeclareDeps()
}

configure<com.diffplug.gradle.spotless.SpotlessExtensionPredeclare> {
    kotlin {
        ktlint()
    }
}


subprojects {
    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt")
            targetExclude("bin/**/*.kt")
            ktlint().editorConfigOverride(
                mapOf(
                    "ij_kotlin_allow_trailing_comma" to true,
                    "ij_kotlin_allow_trailing_comma_on_call_site" to true
                )
            )
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
        }
    }
}
