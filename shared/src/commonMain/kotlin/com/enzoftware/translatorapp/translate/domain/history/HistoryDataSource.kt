package com.enzoftware.translatorapp.translate.domain.history

import com.enzoftware.translatorapp.core.util.CommonFlow
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

interface HistoryDataSource {
    fun getHistory(coroutineContext: CoroutineContext): CommonFlow<List<HistoryItem>>
    suspend fun insertHistoryItem(item: HistoryItem)
}