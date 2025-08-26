package com.enzoftware.translatorapp.translate.data.history

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.enzoftware.translatorapp.TranslatorDatabase
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.history.HistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SqlDelightHistoryDataSource(
    db: TranslatorDatabase,
) : HistoryDataSource {
    private val queries = db.translateQueries

    override fun getHistory(coroutineContext: CoroutineContext): Flow<List<HistoryItem>> {
        return queries
            .getHistory()
            .asFlow()
            .mapToList(coroutineContext)
            .map { history ->
                history.map { it.toHistoryItem() }
            }
    }

    override suspend fun insertHistoryItem(item: HistoryItem) {
        queries.insertHistoryEntity(
            id = item.id,
            fromLanguageCode = item.fromLanguageCode,
            fromText = item.fromText,
            toLanguage = item.toLanguageCode,
            toText = item.toText,
            timestamp = Clock.System.now().toEpochMilliseconds(),
        )
    }
}