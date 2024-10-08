package com.sermilion.ktor

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

/**
 * Configure base Detekt reporting settings
 */
internal fun Project.configureDetekt() {
    val reportMerge: TaskProvider<ReportMergeTask> =
        rootProject.registerMaybe("detektReportMerge") {
            description = "Runs merge of all detekt reports into single one"
            output.set(rootProject.layout.buildDirectory.file("reports/detekt/merged.xml"))
        }

    configure<DetektExtension> {
        //Check for module specific detekt config
        val moduleConfig = file("$projectDir/detekt.yml")
        //If the module defines a specific detekt config, we attach it to the main configuration
        if (moduleConfig.exists()) {
            config.setFrom("$rootDir/config/detekt/detekt.yml", moduleConfig)
        }
        baseline = file("$rootDir/config/detekt_baseline.xml")
        basePath = rootDir.absolutePath
        buildUponDefaultConfig = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = JavaVersion.VERSION_11.toString()
//        exclude("**/build-logic/**")
        exclude("**/tracking/ampli/client**")
        reports {
            // observe findings in your browser with structure and code snippets
            html.required.set(true)
            // checkstyle like format. This is utilized by the merge reports task
            xml.required.set(true)
        }
    }


    /**
     * Finalizes every detekt task with ReportMergeTask
     */
    plugins.withType<DetektPlugin> {
        tasks.withType<Detekt> {
            finalizedBy(reportMerge)
            reportMerge.configure {
                input.from(xmlReportFile)
            }
        }
    }
}
