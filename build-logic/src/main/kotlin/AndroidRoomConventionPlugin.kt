import com.aj.geminiproj.addKoinDependencies
import com.aj.geminiproj.addRoomDependencies
import com.aj.geminiproj.configureAndroidCommon
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target){
            with(pluginManager){
                apply("com.android.library")
                apply("com.google.devtools.ksp")
            }

            extensions.configure<LibraryExtension>{
                configureAndroidCommon(this)
            }
            addRoomDependencies()
            addKoinDependencies()
        }
    }
}