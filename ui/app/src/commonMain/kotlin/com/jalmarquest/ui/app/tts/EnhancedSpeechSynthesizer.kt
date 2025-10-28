package com.jalmarquest.ui.app.tts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Enhanced text-to-speech system with multiple voice options
 * and special bedtime mode for relaxing narration
 */
class EnhancedSpeechSynthesizer {
    
    enum class VoiceProfile(
        val id: String,
        val displayName: String,
        val pitch: Float,
        val rate: Float,
        val gender: Gender
    ) {
        VOICE_A_MALE("voice_a_male", "Voice A (Male)", 0.9f, 1.0f, Gender.MALE),
        VOICE_B_MALE("voice_b_male", "Voice B (Male)", 1.0f, 0.95f, Gender.MALE),
        VOICE_A_FEMALE("voice_a_female", "Voice A (Female)", 1.2f, 1.0f, Gender.FEMALE),
        VOICE_B_FEMALE("voice_b_female", "Voice B (Female)", 1.1f, 1.05f, Gender.FEMALE),
        
        // Special bedtime voice - soothing and slow
        BEDTIME("bedtime", "Bedtime Voice", 0.85f, 0.8f, Gender.NEUTRAL);
        
        enum class Gender { MALE, FEMALE, NEUTRAL }
    }
    
    data class TtsSettings(
        val selectedVoice: VoiceProfile = VoiceProfile.VOICE_A_MALE,
        val isBedtimeModeEnabled: Boolean = false,
        val volume: Float = 1.0f,
        val isEnabled: Boolean = true
    )
    
    private val _settings = MutableStateFlow(TtsSettings())
    val settings: StateFlow<TtsSettings> = _settings.asStateFlow()
    
    private val synthesizer = createPlatformSynthesizer()
    
    fun speak(text: String) {
        val currentSettings = _settings.value
        if (!currentSettings.isEnabled) return
        
        // Use bedtime voice if enabled, otherwise use selected voice
        val voice = if (currentSettings.isBedtimeModeEnabled) {
            VoiceProfile.BEDTIME
        } else {
            currentSettings.selectedVoice
        }
        
        // Apply voice parameters
        synthesizer.setPitch(voice.pitch)
        synthesizer.setSpeechRate(voice.rate)
        synthesizer.setVolume(currentSettings.volume)
        
        // Add pauses for better bedtime narration
        val processedText = if (currentSettings.isBedtimeModeEnabled) {
            addBedtimePauses(text)
        } else {
            text
        }
        
        synthesizer.speak(processedText)
    }
    
    fun setVoice(voice: VoiceProfile) {
        _settings.value = _settings.value.copy(selectedVoice = voice)
    }
    
    fun setBedtimeMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(isBedtimeModeEnabled = enabled)
    }
    
    fun setVolume(volume: Float) {
        _settings.value = _settings.value.copy(volume = volume.coerceIn(0f, 1f))
    }
    
    fun setEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(isEnabled = enabled)
        if (!enabled) {
            stop()
        }
    }
    
    fun stop() {
        synthesizer.stop()
    }
    
    private fun addBedtimePauses(text: String): String {
        // Add slight pauses after sentences for more relaxing narration
        return text
            .replace(". ", ". <pause:500ms> ")
            .replace("! ", "! <pause:500ms> ")
            .replace("? ", "? <pause:500ms> ")
            .replace(", ", ", <pause:200ms> ")
    }
}

/**
 * Platform-specific TTS implementation
 */
expect fun createPlatformSynthesizer(): PlatformSpeechSynthesizer

interface PlatformSpeechSynthesizer {
    fun speak(text: String)
    fun stop()
    fun setPitch(pitch: Float)
    fun setSpeechRate(rate: Float)
    fun setVolume(volume: Float)
}
