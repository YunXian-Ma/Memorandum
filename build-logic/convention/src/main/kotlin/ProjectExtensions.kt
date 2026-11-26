import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String) = findLibrary(alias).get()

/** 由模块路径推导 namespace：:feature:home -> com.jfeng2048.memorandum.feature.home */
internal fun Project.moduleNamespace(): String =
    "com.jfeng2048.memorandum" + path.split(":").filter { it.isNotEmpty() }
        .joinToString("") { "." + it.replace("-", "") }
