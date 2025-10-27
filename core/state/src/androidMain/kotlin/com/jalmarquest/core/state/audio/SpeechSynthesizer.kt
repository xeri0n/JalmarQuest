package com.jalmarquest.core.state.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

private object SpeechSynthesizerContextHolder {
    private var context: Context? = null

    fun provide(context: Context) {
        this.context = context.applicationContext
    }

    fun require(): Context = context
        ?: error("SpeechSynthesizer requires a provided Context before instantiation")

    fun clear() {
        context = null
    }
}

fun createSpeechSynthesizer(context: Context): SpeechSynthesizer {
    SpeechSynthesizerContextHolder.provide(context)
    return SpeechSynthesizer()
}

actual class SpeechSynthesizer actual constructor() {
    private val tag = "SpeechSynthesizer"
    private val utteranceCounter = AtomicInteger()
    private val pendingUtterances = ArrayDeque<String>()
    private val lock = Any()
    private val textToSpeech: TextToSpeech

    @Volatile
    private var initialized = false

    init {
        val context = SpeechSynthesizerContextHolder.require()
        textToSpeech = TextToSpeech(context) { status ->
            synchronized(lock) {
                initialized = status == TextToSpeech.SUCCESS
                if (!initialized) {
                    pendingUtterances.clear()
                    Log.e(tag, "TextToSpeech initialization failed with status=$status")
                    return@synchronized
                }

                val locale = context.resources.configuration.locales?.get(0) ?: Locale.getDefault()
                val result = textToSpeech.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(tag, "Locale $locale not supported, falling back to default")
                    textToSpeech.language = Locale.getDefault()
                }

                while (pendingUtterances.isNotEmpty()) {
                    speakInternal(pendingUtterances.removeFirst())
                }
            }
        }
    }

    actual fun speak(text: String) {
        synchronized(lock) {
            if (!initialized) {
                pendingUtterances.addLast(text)
                Log.d(tag, "Queueing utterance while TTS initializes")
                return
            }
            speakInternal(text)
        }
    }

    private fun speakInternal(text: String) {
        val utteranceId = utteranceCounter.incrementAndGet().toString()
        val result = textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
        if (result == TextToSpeech.ERROR) {
            Log.e(tag, "Failed to speak utterance $utteranceId: $text")
        }
    }

    actual fun shutdown() {
        synchronized(lock) {
            pendingUtterances.clear()
            initialized = false
        }
        textToSpeech.stop()
        textToSpeech.shutdown()
        SpeechSynthesizerContextHolder.clear()
        Log.d(tag, "TextToSpeech shutdown complete")
    }
}
