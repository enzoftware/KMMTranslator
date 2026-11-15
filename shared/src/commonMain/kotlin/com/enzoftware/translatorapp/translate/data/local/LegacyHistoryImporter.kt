package com.enzoftware.translatorapp.translate.data.local

import androidx.sqlite.SQLiteDriver

/** The SQLDelight database file used before the Room migration (`translator.db`). */
interface LegacyDatabaseFile {
    val path: String
    fun exists(): Boolean

    /** Deletes the database and its `-journal` / `-wal` / `-shm` companions. */
    fun delete()

    companion object {
        const val FILE_NAME = "translator.db"
    }
}

/**
 * Copies translation history from the old SQLDelight database into Room once, then deletes the old
 * file.
 *
 * Rows keep their original ids and Room inserts with REPLACE, so an import interrupted between the
 * copy and the delete is simply repeated on the next launch without creating duplicates.
 */
fun interface LegacyHistoryImport {
    suspend fun importIfPresent()
}

class LegacyHistoryImporter(
    private val driver: SQLiteDriver,
    private val legacyFile: LegacyDatabaseFile,
    private val dao: HistoryDao,
) : LegacyHistoryImport {
    override suspend fun importIfPresent() {
        if (!legacyFile.exists()) return
        val rows = readLegacyRows()
        if (rows.isNotEmpty()) dao.upsertAll(rows)
        legacyFile.delete()
    }

    private fun readLegacyRows(): List<HistoryEntity> = driver.open(legacyFile.path).use { connection ->
        val hasTable = connection.prepare(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'historyEntity'",
        ).use { it.step() }
        if (!hasTable) return@use emptyList()

        connection.prepare(
            "SELECT id, fromLanguageCode, fromText, toLanguage, toText, timestamp FROM historyEntity",
        ).use { statement ->
            buildList {
                while (statement.step()) {
                    add(
                        HistoryEntity(
                            id = statement.getLong(0),
                            fromLanguageCode = statement.getText(1),
                            fromText = statement.getText(2),
                            toLanguage = statement.getText(3),
                            toText = statement.getText(4),
                            timestamp = statement.getLong(5),
                        ),
                    )
                }
            }
        }
    }
}
