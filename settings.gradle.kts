import org.gradle.api.initialization.resolve.RepositoriesMode
pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	}
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	}
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "JalmarQuest"

include(
	":core:model",
	":core:state",
	":core:di",
	":feature:eventengine",
	":feature:nest",
	":feature:explore",
	":feature:systemic",
	":feature:hub",
	":feature:activities",
	":feature:skills",
	":ui:app",
	":app:android",
	":app:desktop",
	":backend:database",
	":backend:aidirector"
)