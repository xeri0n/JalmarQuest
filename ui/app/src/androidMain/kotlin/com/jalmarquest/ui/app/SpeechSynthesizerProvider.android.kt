package com.jalmarquest.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.jalmarquest.core.state.audio.SpeechSynthesizer
import com.jalmarquest.core.state.audio.createSpeechSynthesizer

@Composable
actual fun rememberSpeechSynthesizer(): SpeechSynthesizer {
    val context = LocalContext.current
    val synthesizer = remember(context.applicationContext) {
        createSpeechSynthesizer(context.applicationContext)
    }
    DisposableEffect(synthesizer) {
        onDispose { synthesizer.shutdown() }
    }
    return synthesizer
}
