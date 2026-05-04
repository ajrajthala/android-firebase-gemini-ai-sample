package com.aj.geminiproj

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.addAndroidCoreDependencies() {
    dependencies {
        add("implementation", libs.findLibrary("androidx.core.ktx").get())
        add("implementation", libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
    }
}

internal fun Project.addCoroutineDependencies() {
    dependencies {
        add("implementation", libs.findLibrary("kotlinx.coroutines.android").get())
        add("implementation", libs.findLibrary("kotlinx.coroutines.core").get())
    }
}

internal fun Project.addKoinDependencies(){
    dependencies {
        add("implementation", libs.findLibrary("koin.android").get())
        add("implementation", libs.findLibrary("koin.androidx.compose").get())
    }
}

internal fun Project.addRoomDependencies(){
    dependencies {
        add("implementation", libs.findLibrary("androidx.room.runtime").get())
        add("implementation", libs.findLibrary("androidx.room.ktx").get())
        add("ksp", libs.findLibrary("androidx.room.compiler").get())
    }
}

internal fun Project.addCommonTestDependencies() {
    dependencies {
        add("testImplementation", libs.findLibrary("junit").get())
        add("androidTestImplementation", libs.findLibrary("androidx.junit").get())
        add("androidTestImplementation", libs.findLibrary("androidx.espresso.core").get())
    }
}