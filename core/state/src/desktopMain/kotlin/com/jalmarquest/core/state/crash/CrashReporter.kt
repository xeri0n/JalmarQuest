package com.jalmarquest.core.state.crash

/**
 * Desktop JVM implementation of CrashReporter using System.err for Phase 1.
 * Future iterations can integrate Sentry or similar services.
 */
actual class CrashReporter {
    private val metadata = mutableMapOf<String, String>()
    
    actual fun logException(throwable: Throwable, context: String) {
        val fullContext = buildContext(context)
        System.err.println("[JALMAR-CRASH] Non-fatal exception: $fullContext")
        throwable.printStackTrace(System.err)
    }
    
    actual fun logFatalCrash(throwable: Throwable, context: String) {
        val fullContext = buildContext(context)
        System.err.println("[JALMAR-CRASH] *** FATAL CRASH ***: $fullContext")
        throwable.printStackTrace(System.err)
        System.err.flush()
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
        println("[JALMAR-CRASH] Crash reporter exception handlers installed")
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
}
