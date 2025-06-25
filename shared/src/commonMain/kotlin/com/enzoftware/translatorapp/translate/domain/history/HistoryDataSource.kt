package com.enzoftware.translatorapp.translate.domain.history

import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

interface HistoryDataSource {
    fun getHistory(coroutineContext: CoroutineContext): Flow<List<HistoryItem>>
    suspend fun insertHistoryItem(item: HistoryItem)
}