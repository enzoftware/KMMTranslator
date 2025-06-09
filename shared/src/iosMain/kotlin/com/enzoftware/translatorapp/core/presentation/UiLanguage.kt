package com.enzoftware.translatorapp.core.presentation

import com.enzoftware.translatorapp.core.domain.language.Language

actual class UiLanguage(actual val language: Language, val imagePath: String?) {
    actual companion object {
        actual fun byCode(code: String): UiLanguage {
            return allLanguages.find { it.language.code == code } ?: throw IllegalArgumentException("No language with code $code")
        }

        actual val allLanguages: List<UiLanguage>
            get() = Language.entries.map { language ->
                UiLanguage(
                    language = language,
                    imagePath = language.name.lowercase()
                )
            }
    }

}