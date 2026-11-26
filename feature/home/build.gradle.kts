plugins { alias(libs.plugins.memorandum.android.feature) }

dependencies {
    implementation(projects.core.floatingtheme)
    implementation(projects.service.floating)
}
