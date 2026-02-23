package com.enzoftware.translatorapp.di

import com.enzoftware.translatorapp.TranslatorDatabase
import com.enzoftware.translatorapp.translate.data.history.SqlDelightHistoryDataSource
import com.enzoftware.translatorapp.translate.data.local.DatabaseDriverFactory
import com.enzoftware.translatorapp.translate.data.remote.HttpClientFactory
import com.enzoftware.translatorapp.translate.data.translate.KtorTranslateClient
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.translate.TranslateClient
import com.enzoftware.translatorapp.translate.domain.translate.TranslateUseCase
import io.ktor.client.HttpClient

class AppModule {
    val historyDataSource: HistoryDataSource by lazy {
        SqlDelightHistoryDataSource(
            TranslatorDatabase(
                DatabaseDriverFactory().create()
            )
        )
    }

    private val translateClient: TranslateClient by lazy {
        KtorTranslateClient(
            HttpClientFactory().create()
        )
    }

    val translateUseCase: TranslateUseCase by lazy {
        TranslateUseCase(translateClient, historyDataSource)
    }
}