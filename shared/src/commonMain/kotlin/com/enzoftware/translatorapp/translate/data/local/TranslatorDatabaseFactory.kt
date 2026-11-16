package com.enzoftware.translatorapp.translate.data.local

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.enzoftware.translatorapp.translate.data.history.RoomHistoryDataSource
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource

/** Platform entry point: where the Room database and the legacy SQLDelight file live. */
expect class TranslatorDatabaseFactory {
    fun databaseBuilder(): RoomDatabase.Builder<TranslatorDatabase>
    fun legacyDatabase(): LegacyDatabaseFile
}

/** Builds the app's [HistoryDataSource], importing any pre-Room history on first use. */
fun TranslatorDatabaseFactory.createHistoryDataSource(): HistoryDataSource {
    val dao = databaseBuilder().buildTranslatorDatabase().historyDao()
    return RoomHistoryDataSource(
        dao = dao,
        legacyImporter = LegacyHistoryImporter(BundledSQLiteDriver(), legacyDatabase(), dao),
    )
}
