package com.enzoftware.translatorapp.translate.data.local

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AndroidHistoryDatabaseTest : HistoryDatabaseContract() {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    override fun inMemoryDatabaseBuilder(): RoomDatabase.Builder<TranslatorDatabase> =
        Room.inMemoryDatabaseBuilder<TranslatorDatabase>(context)

    override fun fileDatabaseBuilder(path: String): RoomDatabase.Builder<TranslatorDatabase> =
        Room.databaseBuilder<TranslatorDatabase>(context, path)

    override fun scratchFile(name: String): LegacyDatabaseFile = object : LegacyDatabaseFile {
        private val file = File(context.cacheDir, name)
        override val path: String = file.absolutePath
        override fun exists() = file.exists()
        override fun delete() {
            listOf("", "-journal", "-wal", "-shm").forEach { File(file.path + it).delete() }
        }
    }
}
