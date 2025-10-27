package com.jalmarquest.backend.aidirector.deploy

import com.jalmarquest.backend.aidirector.AiDirectorConfig
import com.jalmarquest.backend.aidirector.AiDirectorFactory
import com.jalmarquest.backend.aidirector.AiDirectorService
import com.jalmarquest.backend.aidirector.GeminiPromptBuilder
import com.jalmarquest.backend.aidirector.PromptDispatchObserver
import com.jalmarquest.core.state.analytics.AnalyticsLogger
import com.jalmarquest.feature.eventengine.ChapterEventProvider
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Contains the default Koin wiring for the AI Director so backend entry points can bootstrap
 * production or sandbox deployments with a single call.
 */
object AiDirectorDeployment {
    data class DeploymentHandles(
        val koin: Koin,
        val service: AiDirectorService,
        val chapterEventProvider: ChapterEventProvider
    )

    fun createModule(
        config: AiDirectorConfig,
        promptBuilder: GeminiPromptBuilder = GeminiPromptBuilder(),
        analyticsLogger: AnalyticsLogger = AnalyticsLogger(),
        observer: PromptDispatchObserver = PromptDispatchObserver { _, _, _ -> },
        json: Json = Json { ignoreUnknownKeys = true }
    ): Module = module {
        single { config }
        single { promptBuilder }
        single { analyticsLogger }
        single { observer }
        single { json }

        single {
            AiDirectorFactory.createService(
                config = get(),
                promptBuilder = get(),
                analyticsLogger = get(),
                observer = get(),
                json = get()
            )
        }

        single {
            AiDirectorFactory.createChapterEventProvider(
                config = get(),
                promptBuilder = get(),
                analyticsLogger = get(),
                observer = get(),
                json = get()
            )
        }
    }

    fun bootstrap(
        env: Map<String, String> = System.getenv(),
        promptBuilder: GeminiPromptBuilder = GeminiPromptBuilder(),
        analyticsLogger: AnalyticsLogger = AnalyticsLogger(),
        observer: PromptDispatchObserver = PromptDispatchObserver { _, _, _ -> },
        json: Json = Json { ignoreUnknownKeys = true }
    ): DeploymentHandles {
        val config = AiDirectorConfig.fromEnvironment(env)
    val deploymentModule = createModule(config, promptBuilder, analyticsLogger, observer, json)
    val koin = startKoin { modules(deploymentModule) }.koin
        val service: AiDirectorService = koin.get()
        val provider: ChapterEventProvider = koin.get()
        return DeploymentHandles(koin, service, provider)
    }
}
