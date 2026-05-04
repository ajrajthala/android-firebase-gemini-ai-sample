package com.aj.geminiproj

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(extension: CommonExtension) {
    extension.apply {
        buildFeatures.apply {
            compose = true
        }

        composeOptions.apply {

        }
    }

    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))

        add("implementation", libs.findLibrary("androidx-compose-ui-tooling").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        add("implementation", libs.findLibrary("androidx-compose-material3").get())
        add("implementation", libs.findLibrary("androidx-compose-material-icons-extended").get())
        add("implementation", libs.findLibrary("androidx-compose-material3-windowSizeClass").get())


        add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
    }
}