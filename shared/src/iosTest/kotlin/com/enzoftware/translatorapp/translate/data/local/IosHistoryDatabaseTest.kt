package com.enzoftware.translatorapp.translate.data.local

import androidx.room3.Room
import androidx.room3.RoomDatabase
import platform.Foundation.NSTemporaryDirectory

class IosHistoryDatabaseTest : HistoryDatabaseContract() {
    override fun inMemoryDatabaseBuilder(): RoomDatabase.Builder<TranslatorDatabase> =
        Room.inMemoryDatabaseBuilder<TranslatorDatabase>()

    override fun scratchLegacyFile(): LegacyDatabaseFile =
        IosLegacyDatabaseFile("${NSTemporaryDirectory()}legacy-translator-test.db")
}
