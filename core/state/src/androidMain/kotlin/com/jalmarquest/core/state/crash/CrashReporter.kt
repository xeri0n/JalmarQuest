package com.jalmarquest.core.state.crash

import android.util.Log

/**
 * Android implementation of CrashReporter using Logcat for Phase 1.
 * Future iterations can integrate Firebase Crashlytics or similar services.
 */
actual class CrashReporter {
    private val metadata = mutableMapOf<String, String>()
    
    actual fun logException(throwable: Throwable, context: String) {
        val fullContext = buildContext(context)
        Log.e(TAG, "Non-fatal exception: $fullContext", throwable)
    }
    
    actual fun logFatalCrash(throwable: Throwable, context: String) {
        val fullContext = buildContext(context)
        Log.wtf(TAG, "FATAL CRASH: $fullContext", throwable)
        // In production, this would flush to crash reporting service
    }
    
    actual fun setCustomKey(key: String, value: String) {
        metadata[key] = value
    }
    
    actual fun installExceptionHandlers() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logFatalCrash(throwable, "Uncaught on thread: ${thread.name}")
            defaultHandler?.uncaughtException(thread, throwable)
        }
        Log.i(TAG, "Crash reporter exception handlers installed")
    }
    
    private fun buildContext(context: String): String {
        val metaString = if (metadata.isNotEmpty()) {
            metadata.entries.joinToString(", ") { "${it.key}=${it.value}" }
        } else {
            ""
        }
        return if (context.isNotEmpty() && metaString.isNotEmpty()) {
            "$context | $metaString"
        } else {
            context + metaString
        }
    }
    
    companion object {
        private const val TAG = "JalmarQuest:Crash"
    }
}
