import org.gradle.api.Project
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object AlphaBuildManager {
    fun configureAlphaBuild(project: Project) {
        project.tasks.register("buildAlpha") {
            group = "release"
            description = "Build optimized Alpha release"
            
            dependsOn(
                ":allTests",
                ":detekt",
                ":app:android:assembleRelease",
                ":app:desktop:packageDistributionForCurrentOS"
            )
            
            doLast {
                val timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
                
                val versionName = "1.0.0-alpha-$timestamp"
                
                println("✅ Alpha Build Complete")
                println("Version: $versionName")
                println("Android APK: app/android/build/outputs/apk/release/")
                println("Desktop JAR: app/desktop/build/compose/jars/")
                
                // Generate release notes
                generateReleaseNotes(versionName)
            }
        }
    }
    
    private fun generateReleaseNotes(version: String) {
        val notes = """
        # JalmarQuest Alpha Release - $version
        
        ## ✅ Validated Systems
        - Core gameplay loop (15+ hours of content)
        - 56+ quests with full narrative
        - Nest building & customization
        - Companion system with passive income
        - Crafting with 50+ recipes
        - AI Director dynamic events
        - Save/load system with migration support
        - Localization (EN/NO/EL)
        
        ## 🎮 Known Issues
        - Performance may vary on low-end devices
        - Some particle effects disabled for optimization
        
        ## 📱 Minimum Requirements
        - Android 7.0+ / iOS 12+
        - 2GB RAM
        - 500MB storage
        
        """.trimIndent()
        
        File("RELEASE_NOTES.md").writeText(notes)
    }
}
