import com.yappeer.ktor.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.repositories

class KtorLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm" )
                apply("yappeer.detekt")
                apply("org.jetbrains.kotlin.plugin.serialization")

                dependencies {
                    add("implementation", libs.findLibrary("koin.ktor").get())
                    add("implementation", libs.findLibrary("logback").get())

                    add("testImplementation", kotlin("test"))
                    add("testImplementation", libs.findLibrary("jetbrains.kotlin.junit").get())
                    add("testImplementation", libs.findLibrary("jetbrains.kotlin.test").get())
                    add("testImplementation", libs.findLibrary("ktor.client.contentnegotiation").get())
                    add("testImplementation", libs.findLibrary("ktor.server.testhost").get())
                    add("testImplementation", libs.findLibrary("ktor.server.testhotsjvm").get())
                }
            }

            repositories {
                mavenCentral()
            }
        }
    }
}
