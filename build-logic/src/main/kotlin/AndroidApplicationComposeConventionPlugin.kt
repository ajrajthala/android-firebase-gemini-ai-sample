import com.aj.geminiproj.BuildConstants
import com.aj.geminiproj.addCommonTestDependencies
import com.aj.geminiproj.configureAndroidBuildTypes
import com.aj.geminiproj.configureAndroidCommon
import com.aj.geminiproj.configureAndroidCompose
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target){
            with(pluginManager){
                apply("com.android.application")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension>{
                configureAndroidCommon(this)
                configureAndroidCompose(this)
                configureAndroidBuildTypes(this)
//                configureAndroidFlavors(this)

                defaultConfig{
                    applicationId = BuildConstants.APP_ID
                    targetSdk = BuildConstants.TARGET_SDK
                    versionCode = BuildConstants.VERSION_CODE
                    versionName = BuildConstants.VERSION_NAME

                    testInstrumentationRunner = BuildConstants.TEST_INSTRUMENTATION_RUNNER
                }
            }
            addCommonTestDependencies()
        }
    }
}