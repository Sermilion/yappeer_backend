import com.sermilion.ktor.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class KtorLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("sermilion.detekt")
                apply("org.jetbrains.kotlin.plugin.serialization")

                dependencies {
                    add("implementation", libs.findLibrary("koin.ktor").get())
                    add("implementation", libs.findLibrary("logback").get())
                    add("testImplementation", kotlin("test"))
                }
            }
        }
    }
}
