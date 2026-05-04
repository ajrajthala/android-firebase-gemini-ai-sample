plugins {
    alias(libs.plugins.geminiproj.android.feature)
}

android {
    namespace = "com.aj.geminiproj.features.text"
}

dependencies {
    implementation(project(":core:ai"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
}