@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.enzoftware.translatorapp.translate.data.local

import androidx.room3.Room
import androidx.room3.RoomDatabase
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual class TranslatorDatabaseFactory {

    actual fun databaseBuilder(): RoomDatabase.Builder<TranslatorDatabase> =
        Room.databaseBuilder<TranslatorDatabase>(name = "${databasesDirectory()}/${TranslatorDatabase.FILE_NAME}")

    actual fun legacyDatabase(): LegacyDatabaseFile =
        IosLegacyDatabaseFile("${databasesDirectory()}/${LegacyDatabaseFile.FILE_NAME}")
}

/**
 * `<Application Support>/databases`: where SQLDelight's NativeSqliteDriver (via SQLiter) kept
 * `translator.db`. The Room database sits beside it. Application Support is backed up but not
 * exposed in the Files app, unlike Documents.
 */
internal fun databasesDirectory(): String {
    val applicationSupport = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
        .first() as String
    val directory = "$applicationSupport/databases"
    val fileManager = NSFileManager.defaultManager
    if (!fileManager.fileExistsAtPath(directory)) {
        fileManager.createDirectoryAtPath(directory, withIntermediateDirectories = true, attributes = null, error = null)
    }
    return directory
}

internal class IosLegacyDatabaseFile(override val path: String) : LegacyDatabaseFile {
    override fun exists(): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)

    override fun delete() {
        val fileManager = NSFileManager.defaultManager
        listOf("", "-journal", "-wal", "-shm").forEach { suffix ->
            val file = path + suffix
            if (fileManager.fileExistsAtPath(file)) fileManager.removeItemAtPath(file, error = null)
        }
    }
}
