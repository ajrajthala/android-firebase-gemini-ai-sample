plugins {
    alias(libs.plugins.geminiproj.android.library)
}

android {
    namespace = "com.aj.geminiproj.core.common"
}
dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.core)
    implementation(libs.koin.android)
}