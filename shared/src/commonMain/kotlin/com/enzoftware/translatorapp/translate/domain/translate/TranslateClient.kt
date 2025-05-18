package com.enzoftware.translatorapp.translate.domain.translate

import Language

interface TranslateClient {
    suspend fun translate(fromLanguage: Language, fromText: String, toLanguage: Language): String

}