package com.jalmarquest.core.di

import com.jalmarquest.core.state.crash.CrashReporter

/**
 * Resolve the singleton CrashReporter instance from Koin.
 */
fun resolveCrashReporter(): CrashReporter {
    return requireKoin().get()
}
