package com.enzoftware.translatorapp.translate.data.remote

import com.enzoftware.translatorapp.core.domain.language.Language
import com.enzoftware.translatorapp.translate.domain.translate.TranslateClient
import com.enzoftware.translatorapp.translate.domain.translate.TranslateError
import com.enzoftware.translatorapp.translate.domain.translate.TranslateException
import kotlinx.coroutines.delay

class FakeTranslateClient : TranslateClient {

    var translatedText = "test translation"
    var shouldReturnError = false
    var translateError = TranslateError.UNKNOWN_ERROR

    override suspend fun translate(
        fromLanguage: Language,
        fromText: String,
        toLanguage: Language,
    ): String {
        // A small delay ensures the loading state is observable by the test collector
        // before the result state is emitted (StateFlow conflation prevention).
        delay(1L)
        if (shouldReturnError) throw TranslateException(translateError)
        return translatedText
    }
}