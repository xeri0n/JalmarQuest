plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.moko.resources) apply false
}

allprojects {
    group = "com.jalmarquest"
    version = "0.1.0"
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}