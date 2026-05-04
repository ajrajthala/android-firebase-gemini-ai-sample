plugins {
    alias(libs.plugins.geminiproj.android.room)
}

android {
    namespace = "com.aj.geminiproj.core.data"
}

dependencies {
    implementation(project(":core:model"))
}