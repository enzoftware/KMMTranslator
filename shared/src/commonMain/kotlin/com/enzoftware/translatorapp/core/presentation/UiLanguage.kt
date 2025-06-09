package com.enzoftware.translatorapp.core.presentation

import com.enzoftware.translatorapp.core.domain.language.Language

expect class UiLanguage {
    val language: Language
    companion object {
        fun byCode(code: String): UiLanguage
        val allLanguages: List<UiLanguage>
    }
}