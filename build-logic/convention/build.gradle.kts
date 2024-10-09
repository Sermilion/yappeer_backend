import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.yappeer.ktor.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
    }
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
}


tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("ktorLibrary") {
            id = "yappeer.ktor.library"
            implementationClass = "KtorLibraryConventionPlugin"
        }
        register("detekt") {
            id = "yappeer.detekt"
            implementationClass = "DetektConventionPlugin"
        }
    }
}
