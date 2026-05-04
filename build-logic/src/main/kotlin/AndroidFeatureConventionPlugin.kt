import com.aj.geminiproj.addAndroidCoreDependencies
import com.aj.geminiproj.addCommonTestDependencies
import com.aj.geminiproj.addCoroutineDependencies
import com.aj.geminiproj.addKoinDependencies
import com.aj.geminiproj.configureAndroidCommon
import com.aj.geminiproj.configureAndroidCompose
import com.aj.geminiproj.libs
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.Actions.with
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<LibraryExtension> {
                configureAndroidCommon(this)
                configureAndroidCompose(this)
            }

            addAndroidCoreDependencies()
            addCoroutineDependencies()
            addKoinDependencies()
            addCommonTestDependencies()

            dependencies{
                add("implementation", libs.findLibrary("nav-compose").get())
//                add("implementation", libs.findLibrary("androidx-compose-material3-windowSizeClass").get())
            }
        }
    }
}