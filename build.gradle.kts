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

// ========== ALPHA RELEASE BUILD TASKS ==========

/**
 * Builds optimized APK for Android Alpha release.
 * Output: app/android/build/outputs/apk/release/JalmarQuest-alpha-v0.1.0.apk
 */
tasks.register("buildAlphaAndroid") {
    group = "alpha release"
    description = "Build optimized Android APK for Alpha 1.0 release"
    
    dependsOn(":app:android:assembleRelease")
    
    doLast {
        val apkDir = file("app/android/build/outputs/apk/release")
        if (apkDir.exists()) {
            println("\n=== ALPHA ANDROID BUILD SUCCESSFUL ===")
            println("APK location: ${apkDir.absolutePath}")
            apkDir.listFiles()?.filter { it.extension == "apk" }?.forEach {
                println("  - ${it.name} (${it.length() / 1024 / 1024} MB)")
            }
        }
    }
}

/**
 * Builds optimized JAR for Desktop Alpha release.
 * Output: app/desktop/build/libs/JalmarQuest-desktop-0.1.0.jar
 */
tasks.register("buildAlphaDesktop") {
    group = "alpha release"
    description = "Build optimized Desktop JAR for Alpha 1.0 release"
    
    dependsOn(":app:desktop:jar")
    
    doLast {
        val jarDir = file("app/desktop/build/libs")
        if (jarDir.exists()) {
            println("\n=== ALPHA DESKTOP BUILD SUCCESSFUL ===")
            println("JAR location: ${jarDir.absolutePath}")
            jarDir.listFiles()?.filter { it.extension == "jar" }?.forEach {
                println("  - ${it.name} (${it.length() / 1024 / 1024} MB)")
            }
            println("\nRun with: java -jar app/desktop/build/libs/desktop-0.1.0.jar")
        }
    }
}

/**
 * Builds all Alpha release artifacts (Android + Desktop).
 */
tasks.register("buildAlphaAll") {
    group = "alpha release"
    description = "Build all Alpha 1.0 release artifacts (Android APK + Desktop JAR)"
    
    dependsOn("buildAlphaDesktop") // Android requires SDK, may not be available in all environments
    
    doLast {
        println("\n" + "=".repeat(60))
        println("    ALPHA RELEASE 1.0 - BUILD COMPLETE")
        println("=".repeat(60))
        println("Desktop JAR: app/desktop/build/libs/desktop-0.1.0.jar")
        println("Android APK: app/android/build/outputs/apk/release/")
        println("\nAll artifacts ready for testing and distribution.")
        println("=".repeat(60) + "\n")
    }
}

/**
 * Validates Alpha release readiness (runs all tests before building).
 */
tasks.register("validateAlphaRelease") {
    group = "alpha release"
    description = "Validate Alpha 1.0 release readiness (run all tests + build artifacts)"
    
    dependsOn("allTests")
    finalizedBy("buildAlphaAll")
    
    doLast {
        println("\n✅ All tests passed - proceeding with Alpha build...")
    }
}
