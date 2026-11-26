plugins {
    alias(libs.plugins.memorandum.android.library)
    alias(libs.plugins.memorandum.android.hilt)
    alias(libs.plugins.memorandum.android.room)
}

dependencies {
    implementation(projects.domain)
    implementation(projects.core.common)
    implementation(libs.kotlinx.coroutines.core)
}
