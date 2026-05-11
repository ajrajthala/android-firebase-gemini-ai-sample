plugins {
    alias(libs.plugins.geminiproj.android.library)
}

android {
    namespace = "com.aj.geminiproj.tools.contacts"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(libs.koin.android)
}