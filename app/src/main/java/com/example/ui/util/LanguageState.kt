package com.example.ui.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object LanguageState {
    var currentLanguage by mutableStateOf("EN") // "EN" or "UR"

    fun toggleLanguage() {
        currentLanguage = if (currentLanguage == "EN") "UR" else "EN"
    }

    fun getText(en: String, ur: String): String {
        return if (currentLanguage == "UR") ur else en
    }
}
