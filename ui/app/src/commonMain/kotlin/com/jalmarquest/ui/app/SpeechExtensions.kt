package com.jalmarquest.ui.app

import com.jalmarquest.core.state.audio.SpeechSynthesizer

fun SpeechSynthesizer.safeSpeak(text: String) {
    if (text.isBlank()) return
    runCatching { speak(text) }
        .onFailure { throwable ->
            println("[SpeechSynthesizer] Failed to speak '$text'\n${throwable.stackTraceToString()}")
        }
}
