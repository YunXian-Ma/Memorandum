// 纯 Kotlin：模型、仓库接口、用例。不允许依赖 Android 或任何具体实现
plugins { alias(libs.plugins.memorandum.jvm.library) }

dependencies {
    api(libs.kotlinx.coroutines.core)
}
