plugins {
    alias(libs.plugins.memorandum.android.application)
    alias(libs.plugins.memorandum.android.compose)
    alias(libs.plugins.memorandum.android.hilt)
}

android {
    namespace = "com.jfeng2048.memorandum"

    defaultConfig {
        applicationId = "com.jfeng2048.memorandum"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
}

// app 只做装配：入口、导航图、依赖注入汇总。不放业务代码
dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.navigation)
    implementation(projects.core.floatingtheme)
    implementation(projects.domain)
    implementation(projects.data)
    implementation(projects.feature.home)
    implementation(projects.feature.person)
    implementation(projects.feature.entry)
    implementation(projects.feature.relationship)
    implementation(projects.feature.category)
    implementation(projects.service.floating)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
