package com.enzoftware.translatorapp.translate.data.history

import com.enzoftware.translatorapp.core.util.CommonFlow
import com.enzoftware.translatorapp.core.util.toCommonFlow
import com.enzoftware.translatorapp.translate.data.local.HistoryDao
import com.enzoftware.translatorapp.translate.data.local.HistoryEntity
import com.enzoftware.translatorapp.translate.data.local.LegacyHistoryImport
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.history.HistoryItem
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock

class RoomHistoryDataSource(
    private val dao: HistoryDao,
    private val legacyImporter: LegacyHistoryImport? = null,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : HistoryDataSource {

    private val importLock = Mutex()
    private var importDone = legacyImporter == null

    /**
     * Room runs its queries on its own IO context, so [coroutineContext] (kept for the shared
     * interface) is not needed here.
     */
    override fun getHistory(coroutineContext: CoroutineContext): CommonFlow<List<HistoryItem>> =
        flow {
            importLegacyHistoryOnce()
            emitAll(dao.observeHistory())
        }
            .map { rows -> rows.map { it.toHistoryItem() } }
            .toCommonFlow()

    override suspend fun insertHistoryItem(item: HistoryItem) {
        importLegacyHistoryOnce()
        dao.upsert(
            HistoryEntity(
                id = item.id ?: 0,
                fromLanguageCode = item.fromLanguageCode,
                fromText = item.fromText,
                toLanguage = item.toLanguageCode,
                toText = item.toText,
                timestamp = now(),
            ),
        )
    }

    /** Runs the SQLDelight → Room import before the first read or write, at most once. */
    private suspend fun importLegacyHistoryOnce() {
        if (importDone) return
        importLock.withLock {
            if (importDone) return
            // A failed import keeps the old file, so it is retried on the next launch instead of
            // blocking history for this session.
            runCatching { legacyImporter?.importIfPresent() }
            importDone = true
        }
    }
}

private fun HistoryEntity.toHistoryItem() = HistoryItem(
    id = id,
    fromLanguageCode = fromLanguageCode,
    fromText = fromText,
    toLanguageCode = toLanguage,
    toText = toText,
)
