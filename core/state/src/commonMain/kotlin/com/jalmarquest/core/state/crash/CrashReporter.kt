package com.jalmarquest.core.state.crash

/**
 * Platform-agnostic crash reporter interface for logging uncaught exceptions
 * and critical errors. Implementations should integrate with platform-specific
 * crash reporting services (e.g., Firebase Crashlytics, Sentry) or fallback
 * to console/file logging.
 */
expect class CrashReporter() {
    /**
     * Log a non-fatal exception that was caught and handled.
     * 
     * @param throwable The exception to log
     * @param context Additional context about when/where the error occurred
     */
    fun logException(throwable: Throwable, context: String = "")
    
    /**
     * Log a fatal crash that will terminate the application.
     * Platform implementations should flush logs immediately.
     * 
     * @param throwable The fatal exception
     * @param context Additional crash context
     */
    fun logFatalCrash(throwable: Throwable, context: String = "")
    
    /**
     * Set custom key-value pairs to be attached to all future crash reports.
     * Useful for user ID, app version, feature flags, etc.
     * 
     * @param key The metadata key
     * @param value The metadata value
     */
    fun setCustomKey(key: String, value: String)
    
    /**
     * Install uncaught exception handlers for the current platform.
     * Should be called early in application startup.
     */
    fun installExceptionHandlers()
}
