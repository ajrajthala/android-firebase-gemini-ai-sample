plugins {
    alias(libs.plugins.geminiproj.android.feature)
    alias(libs.plugins.geminiproj.kotlin.serialization)
}

android {
    namespace = "com.aj.geminiproj.features.chat"
}

dependencies {
    implementation(project(":core:ai"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))

    implementation(libs.coil.compose)
}