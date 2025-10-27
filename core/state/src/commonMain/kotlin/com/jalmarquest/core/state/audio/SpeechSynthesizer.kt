package com.jalmarquest.core.state.audio

expect class SpeechSynthesizer() {
    fun speak(text: String)
    fun shutdown()
}
