plugins {
    alias(libs.plugins.geminiproj.android.library)
}

android {
    namespace = "com.aj.geminiproj.tools.calendar"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(libs.koin.android)
}