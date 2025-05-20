package com.enzoftware.translatorapp.translate.domain.translate

import com.enzoftware.translatorapp.core.domain.language.Language

interface TranslateClient {
    suspend fun translate(fromLanguage: Language, fromText: String, toLanguage: Language): String

}