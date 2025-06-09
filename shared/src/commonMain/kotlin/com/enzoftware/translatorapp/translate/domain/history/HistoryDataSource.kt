package com.enzoftware.translatorapp.translate.domain.history

import com.enzoftware.translatorapp.core.util.CommonFlow
import kotlin.coroutines.CoroutineContext

interface HistoryDataSource {
    fun getHistory(coroutineContext: CoroutineContext): List<HistoryItem>
    suspend fun insertHistoryItem(item: HistoryItem)
}