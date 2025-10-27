# JalmarQuest Alpha Release - ProGuard Rules
# Optimized for Kotlin Multiplatform + Compose + Koin + kotlinx.serialization

# ========== Kotlin & Coroutines ==========
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Kotlin Metadata
-keep class kotlin.Metadata { *; }

# Coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ========== kotlinx.serialization ==========
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes
-keep,includedescriptorclasses class com.jalmarquest.core.model.**$$serializer { *; }
-keepclassmembers class com.jalmarquest.core.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.jalmarquest.core.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ========== Koin DI ==========
-keep class org.koin.** { *; }
-keep class org.koin.core.** { *; }
-keep class org.koin.android.** { *; }

# Keep Koin modules
-keep class com.jalmarquest.core.di.** { *; }

# ========== Compose ==========
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }

-dontwarn androidx.compose.**

# ========== JalmarQuest Core Classes ==========
# Keep all data classes with serialization
-keep @kotlinx.serialization.Serializable class * { *; }

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep platform-specific implementations (expect/actual)
-keep class com.jalmarquest.core.state.auth.** { *; }
-keep class com.jalmarquest.core.state.audio.** { *; }
-keep class com.jalmarquest.core.state.crash.** { *; }
-keep class com.jalmarquest.core.state.analytics.** { *; }
-keep class com.jalmarquest.core.state.monetization.IapService { *; }

# Keep managers (used via reflection/DI)
-keep class com.jalmarquest.core.state.managers.** { *; }
-keep class com.jalmarquest.core.state.GameStateManager { *; }
-keep class com.jalmarquest.core.state.quests.QuestManager { *; }

# Keep state machines
-keep class com.jalmarquest.feature.**.*StateMachine { *; }
-keep class com.jalmarquest.feature.**.*Controller { *; }

# ========== General Android ==========
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ========== Optimization ==========
-optimizationpasses 5
-dontpreverify
-verbose
