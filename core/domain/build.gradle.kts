plugins {
    alias(libs.plugins.yappeer.ktor.library)
}

group = "com.yappeer.core.domain"
version = "unspecified"

dependencies {
    implementation(libs.jetbrains.kotlinx.datetime)
}
