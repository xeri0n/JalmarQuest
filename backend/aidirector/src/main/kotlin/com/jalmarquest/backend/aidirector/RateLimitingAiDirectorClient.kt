package com.jalmarquest.backend.aidirector

import com.jalmarquest.core.model.ChapterEventResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Decorates an [AiDirectorClient] to ensure no more than [config.maxRequests] are executed within the
 * configured rolling window. Calls suspend instead of throwing when the limit is exceeded.
 */
class RateLimitingAiDirectorClient(
    private val delegate: AiDirectorClient,
    private val config: RateLimitingConfig,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val delayFn: suspend (Long) -> Unit = { delay(it) }
) : AiDirectorClient {

    private val mutex = Mutex()
    private val requestTimestamps = ArrayDeque<Long>()

    override suspend fun generateChapterEvent(assembly: PromptAssemblyResult): ChapterEventResponse {
        awaitPermit()
        return delegate.generateChapterEvent(assembly)
    }

    private suspend fun awaitPermit() {
        while (true) {
            val waitDuration = mutex.withLock {
                val now = clock()
                prune(now)
                if (requestTimestamps.size < config.maxRequests) {
                    requestTimestamps.addLast(now)
                    return@withLock null
                }

                val oldest = requestTimestamps.first()
                val intervalEnd = oldest + config.intervalMillis
                val remaining = intervalEnd - now
                if (remaining <= 0) {
                    requestTimestamps.removeFirst()
                    requestTimestamps.addLast(now)
                    return@withLock null
                }
                remaining
            }

            if (waitDuration == null) {
                return
            }

            delayFn(waitDuration)
        }
    }

    private fun prune(now: Long) {
        val minTimestamp = now - config.intervalMillis
        while (requestTimestamps.isNotEmpty() && requestTimestamps.first() < minTimestamp) {
            requestTimestamps.removeFirst()
        }
    }
}
