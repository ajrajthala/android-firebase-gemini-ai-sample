package com.aj.geminiproj

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureAndroidCommon(
    extension: CommonExtension,
) {
    extension.apply {
        compileSdk = BuildConstants.COMPILE_SDK
        defaultConfig.apply {
            minSdk = BuildConstants.MIN_SDK
            testInstrumentationRunner = BuildConstants.TEST_INSTRUMENTATION_RUNNER
            vectorDrawables {
                useSupportLibrary = true
            }

        }
        compileOptions.apply {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17

            isCoreLibraryDesugaringEnabled = true
        }

        buildFeatures.apply {
            aidl = false
            resValues = false
            shaders = false
            buildConfig = false
        }

        packaging.apply {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
                excludes += "/META-INF/LICENSE*"
                excludes += "/META-INF/NOTICE*"
            }
        }

        testOptions.apply {
            unitTests.isReturnDefaultValues = true
            unitTests.isIncludeAndroidResources = true
        }

        lint.apply {
            checkReleaseBuilds = false
            abortOnError = false
            warningsAsErrors = false
        }

    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)

            progressiveMode.set(true)
            freeCompilerArgs.addAll(
                listOf(
                    "-opt-in=kotlin.RequiresOptIn",
                    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-opt-in=kotlinx.coroutines.FlowPreview",
                )
            )
        }
    }

    // Add desugaring dependency
    dependencies {
        add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:2.0.4")
    }
}