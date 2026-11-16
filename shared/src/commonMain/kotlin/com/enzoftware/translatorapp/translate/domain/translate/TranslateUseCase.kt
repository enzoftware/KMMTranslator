package com.enzoftware.translatorapp.translate.domain.translate

import com.enzoftware.translatorapp.core.domain.language.Language
import com.enzoftware.translatorapp.core.util.Resource
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.history.HistoryItem
import kotlinx.coroutines.CancellationException

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
            saveToHistory(fromLanguage, fromText, toLanguage, translatedText)
            Resource.Success(translatedText)
        } catch (e: TranslateException) {
            e.printStackTrace()
            Resource.Error(e)
        }
    }

    /** A history failure (e.g. a pending legacy import) must not hide a translation that succeeded. */
    private suspend fun saveToHistory(
        fromLanguage: Language,
        fromText: String,
        toLanguage: Language,
        translatedText: String,
    ) {
        try {
            historyDataSource.insertHistoryItem(
                HistoryItem(
                    id = null,
                    fromLanguageCode = fromLanguage.code,
                    fromText = fromText,
                    toLanguageCode = toLanguage.code,
                    toText = translatedText,
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
