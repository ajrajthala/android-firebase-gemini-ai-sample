import org.jetbrains.kotlin.gradle.internal.backend.common.serialization.metadata.DynamicTypeDeserializer.id

plugins {
    `kotlin-dsl`
}

group = "com.aj.geminproj.buildlogic"

dependencies {
    implementation("com.android.tools.build:gradle:9.1.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.3.21")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.6")
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "geminiproj.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }

        register("kotlinLibrary") {
            id = "geminiproj.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }

        register("androidApplication") {
            id = "geminiproj.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }

        register("androidCompose") {
            id = "geminiproj.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }

        register("kotlinSerialization") {
            id = "geminiproj.kotlin.serialization"
            implementationClass = "KotlinSerializationConventionPlugin"
        }

        register("androidFeature") {
            id = "geminiproj.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }

        register("androidRoom"){
            id="geminiproj.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }

    }
}