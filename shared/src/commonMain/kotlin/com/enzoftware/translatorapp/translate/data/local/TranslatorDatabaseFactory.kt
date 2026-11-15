package com.enzoftware.translatorapp.translate.data.local

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.enzoftware.translatorapp.phrasebook.data.RoomSavedWordsDataSource
import com.enzoftware.translatorapp.phrasebook.domain.SavedWordsDataSource
import com.enzoftware.translatorapp.translate.data.history.RoomHistoryDataSource
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource

/** Platform entry point: where the Room database and the legacy SQLDelight file live. */
expect class TranslatorDatabaseFactory {
    fun databaseBuilder(): RoomDatabase.Builder<TranslatorDatabase>
    fun legacyDatabase(): LegacyDatabaseFile
}

/**
 * Owns the single [TranslatorDatabase] and the data sources built on it. Create one per app
 * (a Hilt singleton on Android, `AppModule` on iOS).
 */
class TranslatorStorage(factory: TranslatorDatabaseFactory) {
    private val database: TranslatorDatabase = factory.databaseBuilder().buildTranslatorDatabase()

    /** Imports any pre-Room (SQLDelight) history on first use. */
    val historyDataSource: HistoryDataSource = RoomHistoryDataSource(
        dao = database.historyDao(),
        legacyImporter = LegacyHistoryImporter(BundledSQLiteDriver(), factory.legacyDatabase(), database.historyDao()),
    )

    val savedWordsDataSource: SavedWordsDataSource = RoomSavedWordsDataSource(database.savedWordDao())
}
