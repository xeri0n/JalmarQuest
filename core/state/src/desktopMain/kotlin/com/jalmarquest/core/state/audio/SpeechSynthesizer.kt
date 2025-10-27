package com.jalmarquest.core.state.audio

import com.sun.speech.freetts.Voice
import com.sun.speech.freetts.VoiceManager

actual class SpeechSynthesizer actual constructor() {
    private val voice: Voice?

    init {
        if (System.getProperty(FREETTS_VOICES_PROPERTY).isNullOrBlank()) {
            System.setProperty(FREETTS_VOICES_PROPERTY, DEFAULT_VOICE_DIRECTORY)
        }
        voice = VoiceManager.getInstance().getVoice(DEFAULT_VOICE_NAME)?.apply {
            allocate()
        }
    }

    actual fun speak(text: String) {
        if (voice == null) {
            println("[SpeechSynthesizer] Voice not available: $text")
            return
        }
        voice.speak(text)
    }

    actual fun shutdown() {
        voice?.deallocate()
    }

    private companion object {
        const val DEFAULT_VOICE_NAME = "kevin16"
        const val DEFAULT_VOICE_DIRECTORY = "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory"
        const val FREETTS_VOICES_PROPERTY = "freetts.voices"
    }
}
