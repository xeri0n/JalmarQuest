package com.jalmarquest.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.jalmarquest.core.state.audio.SpeechSynthesizer

@Composable
actual fun rememberSpeechSynthesizer(): SpeechSynthesizer {
    val synthesizer = remember { SpeechSynthesizer() }
    DisposableEffect(synthesizer) {
        onDispose { synthesizer.shutdown() }
    }
    return synthesizer
}
