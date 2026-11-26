plugins {
    alias(libs.plugins.memorandum.android.library)
    alias(libs.plugins.memorandum.android.compose)
}

dependencies {
    implementation(projects.domain)
    implementation(projects.core.common)
}
