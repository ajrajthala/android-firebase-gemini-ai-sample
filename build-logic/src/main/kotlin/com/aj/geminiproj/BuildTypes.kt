package com.aj.geminiproj

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project

internal fun Project.configureAndroidBuildTypes(
    extension: ApplicationExtension,
) {
    extension.apply {
        buildTypes {
            release {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }

            debug {
                isMinifyEnabled = false
                // Removed applicationIdSuffix to match google-services.json
                versionNameSuffix = "-DEBUG"
            }
        }
    }
}

internal fun Project.configureAndroidFlavors(
    extension: ApplicationExtension,
) {
    extension.apply {
        flavorDimensions += "environment"

        productFlavors {
            create("dev") {
                dimension = "environment"
                applicationIdSuffix = ".dev"
                versionNameSuffix = "-dev"
            }

            create("prod") {
                dimension = "environment"
            }
        }
    }
}