package com.enzoftware.translatorapp.android.di

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import com.enzoftware.translatorapp.TranslatorDatabase
import com.enzoftware.translatorapp.translate.data.history.SqlDelightHistoryDataSource
import com.enzoftware.translatorapp.translate.data.local.DatabaseDriverFactory
import com.enzoftware.translatorapp.translate.data.remote.HttpClientFactory
import com.enzoftware.translatorapp.translate.data.translate.KtorTranslateClient
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.translate.TranslateClient
import com.enzoftware.translatorapp.translate.domain.translate.TranslateUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttClient(): HttpClient = HttpClientFactory().create()

    @Provides
    @Singleton
    fun provideTranslateClient(httpClient: HttpClient): TranslateClient = KtorTranslateClient(httpClient)

    @Provides
    @Singleton
    fun provideDatabaseDriverFactory(app: Application): SqlDriver = DatabaseDriverFactory(app).create()

    @Provides
    @Singleton
    fun provideHistoryDataSource(sqlDriver: SqlDriver): HistoryDataSource =
        SqlDelightHistoryDataSource(db = TranslatorDatabase.invoke(sqlDriver))

    @Provides
    @Singleton
    fun provideTranslateUseCase(
        client: TranslateClient,
        dataSource: HistoryDataSource
    ): TranslateUseCase = TranslateUseCase(client, dataSource)

}