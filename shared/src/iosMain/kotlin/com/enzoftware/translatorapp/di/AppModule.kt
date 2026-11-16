package com.enzoftware.translatorapp.di

import com.enzoftware.translatorapp.translate.data.local.TranslatorDatabaseFactory
import com.enzoftware.translatorapp.translate.data.local.TranslatorStorage
import com.enzoftware.translatorapp.phrasebook.domain.SavedWordsDataSource
import com.enzoftware.translatorapp.translate.data.remote.HttpClientFactory
import com.enzoftware.translatorapp.translate.data.translate.KtorTranslateClient
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.translate.TranslateClient
import com.enzoftware.translatorapp.translate.domain.translate.TranslateUseCase
import io.ktor.client.HttpClient

class AppModule {
    private val storage: TranslatorStorage by lazy { TranslatorStorage(TranslatorDatabaseFactory()) }

    val historyDataSource: HistoryDataSource by lazy { storage.historyDataSource }

    val savedWordsDataSource: SavedWordsDataSource by lazy { storage.savedWordsDataSource }

    private val translateClient: TranslateClient by lazy {
        KtorTranslateClient(
            HttpClientFactory().create()
        )
    }

    val translateUseCase: TranslateUseCase by lazy {
        TranslateUseCase(translateClient, historyDataSource)
    }
}