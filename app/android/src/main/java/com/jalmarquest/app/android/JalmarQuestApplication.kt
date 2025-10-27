package com.jalmarquest.app.android

import android.app.Application
import com.jalmarquest.core.di.defaultPlayer
import com.jalmarquest.core.di.defaultCharacterAccount
import com.jalmarquest.core.di.initKoin
import com.jalmarquest.core.di.resolveCrashReporter
import com.jalmarquest.core.state.auth.InMemoryGuestAuthGateway
import com.jalmarquest.core.state.auth.installAndroidAuthTokenStorage
import com.jalmarquest.core.state.perf.PerformanceLogger

class JalmarQuestApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		installAndroidAuthTokenStorage(this)
		initKoin(
			initialPlayer = defaultPlayer(),
			initialCharacterAccount = defaultCharacterAccount(),
			guestGateway = InMemoryGuestAuthGateway()
		)
		
		// Install crash reporting early
		val crashReporter = resolveCrashReporter()
		crashReporter.setCustomKey("platform", "Android")
		crashReporter.setCustomKey("app_version", "0.1.0-vertical-slice")
		crashReporter.installExceptionHandlers()
		
		// Log startup completion
		PerformanceLogger.logStartupComplete()
	}
}
