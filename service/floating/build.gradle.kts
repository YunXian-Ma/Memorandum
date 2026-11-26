// 悬浮助手服务：WindowManager + ComposeView。主题渲染来自 core:floatingtheme
plugins {
    alias(libs.plugins.memorandum.android.library)
    alias(libs.plugins.memorandum.android.compose)
    alias(libs.plugins.memorandum.android.hilt)
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.floatingtheme)
    implementation(projects.domain)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.core)
}
