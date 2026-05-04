plugins {
    alias(libs.plugins.geminiproj.android.library)
    alias(libs.plugins.geminiproj.android.compose)
}

android {
    namespace = "com.aj.geminiproj.ui"
}

dependencies {
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material3.windowSizeClass)
}