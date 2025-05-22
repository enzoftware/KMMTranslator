package com.enzoftware.translatorapp.translate.domain.history

import com.enzoftware.translatorapp.core.util.CommonFlow

interface HistoryDataSource {
    suspend fun getHistory(): CommonFlow<List<HistoryItem>>
    suspend fun insertHistoryItem(item: HistoryItem)
}