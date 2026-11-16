package com.enzoftware.translatorapp.translate.data.local

import androidx.room3.ConstructedBy
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

/**
 * One saved translation. Table and column names match the former SQLDelight schema
 * (`historyEntity`, including the `toLanguage` column) so the data model stays recognisable.
 */
@Entity(tableName = "historyEntity")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromLanguageCode: String,
    val fromText: String,
    val toLanguage: String,
    val toText: String,
    val timestamp: Long,
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM historyEntity ORDER BY timestamp DESC")
    fun observeHistory(): Flow<List<HistoryEntity>>

    /** Same id replaces the row, matching the old `INSERT OR REPLACE`. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<HistoryEntity>)
}

@Database(entities = [HistoryEntity::class], version = 1)
@ConstructedBy(TranslatorDatabaseConstructor::class)
abstract class TranslatorDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        /** File name of the Room database. */
        const val FILE_NAME = "parley.db"
    }
}

// The Room compiler generates the actual implementations.
@Suppress("KotlinNoActualForExpect")
expect object TranslatorDatabaseConstructor : RoomDatabaseConstructor<TranslatorDatabase> {
    override fun initialize(): TranslatorDatabase
}

/** Common configuration for every platform: bundled SQLite and IO-bound queries. */
fun RoomDatabase.Builder<TranslatorDatabase>.buildTranslatorDatabase(): TranslatorDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
