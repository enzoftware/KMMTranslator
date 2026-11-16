package com.enzoftware.translatorapp.translate.data.history

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.enzoftware.translatorapp.translate.data.local.HistoryDao
import com.enzoftware.translatorapp.translate.data.local.HistoryEntity
import com.enzoftware.translatorapp.translate.domain.history.HistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test

/** Pure tests of [RoomHistoryDataSource] against an in-memory DAO; Room itself is covered by HistoryDatabaseContract. */
class RoomHistoryDataSourceTest {

    private class FakeHistoryDao(val events: MutableList<String>) : HistoryDao {
        val rows = MutableStateFlow<List<HistoryEntity>>(emptyList())
        private var nextId = 1L

        override fun observeHistory(): Flow<List<HistoryEntity>> {
            events += "read"
            return rows.map { list -> list.sortedByDescending { it.timestamp } }
        }

        override suspend fun upsert(entity: HistoryEntity) {
            events += "write"
            val stored = if (entity.id == 0L) entity.copy(id = nextId++) else entity
            rows.update { list -> list.filterNot { it.id == stored.id } + stored }
        }

        override suspend fun upsertAll(entities: List<HistoryEntity>) = entities.forEach { upsert(it) }
    }

    private val events = mutableListOf<String>()
    private val dao = FakeHistoryDao(events)
    private var clock = 1_000L

    private fun dataSource(import: (suspend () -> Unit)? = null) = RoomHistoryDataSource(
        dao = dao,
        legacyImporter = import?.let { block -> { block() } },
        now = { clock },
    )

    private fun item(from: String, to: String) =
        HistoryItem(id = null, fromLanguageCode = "en", fromText = from, toLanguageCode = "es", toText = to)

    @Test
    fun insertedItemsAreMappedAndNewestFirst() = runTest {
        val source = dataSource()

        source.insertHistoryItem(item("Hello", "Hola"))
        clock = 2_000L
        source.insertHistoryItem(item("Thanks", "Gracias"))

        source.getHistory(EmptyCoroutineContext).test {
            val history = awaitItem()
            assertThat(history.map { it.fromText }).containsExactly("Thanks", "Hello")
            assertThat(history.first()).isEqualTo(
                HistoryItem(id = 2, fromLanguageCode = "en", fromText = "Thanks", toLanguageCode = "es", toText = "Gracias"),
            )
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(dao.rows.value.map { it.timestamp }).containsExactly(1_000L, 2_000L)
    }

    @Test
    fun legacyImportRunsOnceBeforeTheFirstReadOrWrite() = runTest {
        var imports = 0
        val source = dataSource { imports++; events += "import" }

        source.insertHistoryItem(item("Hello", "Hola"))
        source.getHistory(EmptyCoroutineContext).test { awaitItem(); cancelAndIgnoreRemainingEvents() }
        source.insertHistoryItem(item("Thanks", "Gracias"))

        assertThat(imports).isEqualTo(1)
        assertThat(events.first()).isEqualTo("import")
    }

    @Test
    fun aFailingImportDoesNotBlockHistory() = runTest {
        val source = dataSource { error("corrupt legacy file") }

        source.insertHistoryItem(item("Hello", "Hola"))

        source.getHistory(EmptyCoroutineContext).test {
            assertThat(awaitItem().map { it.toText }).containsExactly("Hola")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
