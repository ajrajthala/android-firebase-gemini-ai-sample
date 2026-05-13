plugins {
    alias(libs.plugins.geminiproj.android.application.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.aj.geminiproj"
}

dependencies {
    implementation(project(":core:ai"))
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":tools:contacts"))
    implementation(project(":core:ui"))

    implementation(project(":features:chat"))
    implementation(project(":features:image"))
    implementation(project(":features:text"))

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.nav.compose)

    // Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}