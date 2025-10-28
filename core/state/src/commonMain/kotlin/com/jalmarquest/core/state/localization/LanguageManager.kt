package com.jalmarquest.core.state.localization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages game language settings with English as the guaranteed default
 */
class LanguageManager(
    private val persistenceKey: String = "game_language",
    private val storage: LanguagePreferenceStorage
) {
    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        NORWEGIAN("no", "Norsk"),
        GREEK("el", "Ελληνικά")
    }
    
    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()
    
    init {
        // CRITICAL: Always default to English on first launch
        loadLanguagePreference()
    }
    
    private fun loadLanguagePreference() {
        val savedLanguageCode = storage.getLanguagePreference(persistenceKey)
        
        // Only use saved preference if it exists AND is valid
        val language = if (savedLanguageCode != null) {
            Language.entries.find { it.code == savedLanguageCode } ?: Language.ENGLISH
        } else {
            // First launch - ALWAYS use English regardless of system locale
            Language.ENGLISH
        }
        
        _currentLanguage.value = language
        
        // Save the selection to mark that we've initialized
        if (savedLanguageCode == null) {
            storage.saveLanguagePreference(persistenceKey, Language.ENGLISH.code)
        }
    }
    
    fun setLanguage(language: Language) {
        _currentLanguage.value = language
        storage.saveLanguagePreference(persistenceKey, language.code)
    }
    
    fun isFirstLaunch(): Boolean {
        return storage.getLanguagePreference(persistenceKey) == null
    }
}

/**
 * Platform-specific storage for language preference
 */
expect class LanguagePreferenceStorage() {
    fun getLanguagePreference(key: String): String?
    fun saveLanguagePreference(key: String, value: String)
}
