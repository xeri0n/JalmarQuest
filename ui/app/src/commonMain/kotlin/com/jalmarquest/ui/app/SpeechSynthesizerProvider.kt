package com.jalmarquest.ui.app

import androidx.compose.runtime.Composable
import com.jalmarquest.core.state.audio.SpeechSynthesizer

@Composable
expect fun rememberSpeechSynthesizer(): SpeechSynthesizer
