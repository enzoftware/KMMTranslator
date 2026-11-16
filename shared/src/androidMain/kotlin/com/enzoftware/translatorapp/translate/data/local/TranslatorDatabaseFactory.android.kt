package com.enzoftware.translatorapp.translate.data.local

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

actual class TranslatorDatabaseFactory(context: Context) {
    private val appContext = context.applicationContext

    actual fun databaseBuilder(): RoomDatabase.Builder<TranslatorDatabase> =
        Room.databaseBuilder<TranslatorDatabase>(
            context = appContext,
            name = appContext.getDatabasePath(TranslatorDatabase.FILE_NAME).absolutePath,
        )

    actual fun legacyDatabase(): LegacyDatabaseFile = AndroidLegacyDatabaseFile(appContext)
}

private class AndroidLegacyDatabaseFile(private val context: Context) : LegacyDatabaseFile {
    override val path: String = context.getDatabasePath(LegacyDatabaseFile.FILE_NAME).absolutePath
    override fun exists(): Boolean = context.getDatabasePath(LegacyDatabaseFile.FILE_NAME).exists()

    /** `deleteDatabase` also removes the journal / WAL / SHM companions. */
    override fun delete() {
        context.deleteDatabase(LegacyDatabaseFile.FILE_NAME)
    }
}
