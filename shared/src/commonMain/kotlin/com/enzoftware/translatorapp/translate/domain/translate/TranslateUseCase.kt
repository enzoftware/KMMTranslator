package com.enzoftware.translatorapp.translate.domain.translate

import com.enzoftware.translatorapp.core.domain.language.Language
import com.enzoftware.translatorapp.core.util.Resource
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.history.HistoryItem

class TranslateUseCase(
    private val client: TranslateClient,
    private val historyDataSource: HistoryDataSource,
) {
    suspend fun execute(
        fromLanguage: Language,
        fromText: String,
        toLanguage: Language,
    ): Resource<String> {
        return try {
            val translatedText = client.translate(
                fromLanguage,
                fromText,
                toLanguage
            )
            historyDataSource.insertHistoryItem(
                HistoryItem(
                    id = null,
                    fromLanguageCode = fromLanguage.code,
                    fromText = fromText,
                    toLanguageCode = toLanguage.code,
                    toText = translatedText,
                )
            )
            Resource.Success(translatedText)
        } catch (e: TranslateException) {
            e.printStackTrace()
            Resource.Error(e)
        }
    }
}