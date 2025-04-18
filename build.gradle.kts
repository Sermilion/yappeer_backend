plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.yappeer.ktor.library) apply false
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

// Define Java and Kotlin version for all projects
allprojects {
    // Apply Java version to all projects that have the Java plugin
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }
    
    // Apply Kotlin JVM target to all projects that have the Kotlin plugin
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
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
