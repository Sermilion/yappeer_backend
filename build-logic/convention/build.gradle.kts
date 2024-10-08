import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.sermilion.ktor.buildlogic"

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
            id = "sermilion.ktor.library"
            implementationClass = "KtorLibraryConventionPlugin"
        }
//        register("androidHilt") {
//            id = "sermilion.android.hilt"
//            implementationClass = "AndroidHiltConventionPlugin"
//        }
//        register("androidRoom") {
//            id = "sermilion.android.room"
//            implementationClass = "AndroidRoomConventionPlugin"
//        }
//        register("androidFirebase") {
//            id = "sermilion.ktor.application.firebase"
//            implementationClass = "KtorApplicationFirebaseConventionPlugin"
//        }
        register("detekt") {
            id = "sermilion.detekt"
            implementationClass = "DetektConventionPlugin"
        }
    }
}
