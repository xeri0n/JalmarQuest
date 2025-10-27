package com.jalmarquest.core.state.audio

import platform.AVFoundation.AVSpeechBoundaryImmediate
import platform.AVFoundation.AVSpeechSynthesisVoice
import platform.AVFoundation.AVSpeechSynthesizer
import platform.AVFoundation.AVSpeechUtterance
import platform.AVFoundation.AVSpeechUtteranceDefaultSpeechRate
import platform.Foundation.NSLocale
import platform.Foundation.NSThread
import platform.Foundation.localeIdentifier
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual class SpeechSynthesizer actual constructor() {
    private val synthesizer = AVSpeechSynthesizer()
    private val locale = NSLocale.currentLocale

    actual fun speak(text: String) {
        if (text.isBlank()) return
        runOnMain {
            val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
            val voice = AVSpeechSynthesisVoice.voiceWithLanguage(locale.localeIdentifier)
                ?: AVSpeechSynthesisVoice.voiceWithLanguage("en-US")
            utterance.voice = voice
            utterance.rate = AVSpeechUtteranceDefaultSpeechRate
            synthesizer.speakUtterance(utterance)
        }
    }

    actual fun shutdown() {
        runOnMain {
            synthesizer.stopSpeakingAtBoundary(AVSpeechBoundaryImmediate)
        }
    }

    private fun runOnMain(block: () -> Unit) {
        if (NSThread.isMainThread) {
            block()
        } else {
            dispatch_async(dispatch_get_main_queue(), block)
        }
    }
}
