import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * 功能模块（feature:*）的标准配置：Library + Compose + Hilt，
 * 并默认依赖设计系统、领域层、路由表。功能模块之间不允许互相依赖。
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("memorandum.android.library")
        pluginManager.apply("memorandum.android.compose")
        pluginManager.apply("memorandum.android.hilt")
        dependencies {
            add("implementation", project(":core:designsystem"))
            add("implementation", project(":core:common"))
            add("implementation", project(":core:navigation"))
            add("implementation", project(":domain"))
            add("implementation", libs.library("androidx-lifecycle-runtime-compose"))
            add("implementation", libs.library("androidx-lifecycle-viewmodel-compose"))
            add("implementation", libs.library("androidx-hilt-navigation-compose"))
            add("implementation", libs.library("kotlinx-coroutines-core"))
        }
    }
}
