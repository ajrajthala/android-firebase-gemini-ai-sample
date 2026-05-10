plugins {
    alias(libs.plugins.geminiproj.android.library)
    alias(libs.plugins.geminiproj.kotlin.serialization)
}

android {
    namespace = "com.aj.geminiproj.core.ai"
}

dependencies {

    implementation(project(":core:model"))
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.koin.android)
    implementation(libs.koin.core)
}