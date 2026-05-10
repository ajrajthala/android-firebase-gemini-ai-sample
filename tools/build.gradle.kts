plugins {
    alias(libs.plugins.geminiproj.android.library)
}

android {
    namespace = "com.aj.geminiproj.tools"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
}