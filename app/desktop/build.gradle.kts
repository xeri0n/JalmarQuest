plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    application
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.ui.app)
    implementation(projects.core.di)
    implementation(projects.backend.database)
    implementation(projects.core.model)
    implementation(projects.core.state)
    implementation(projects.feature.eventengine)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(compose.desktop.currentOs)
}

application {
    mainClass.set("com.jalmarquest.app.desktop.DesktopMainKt")
}
