package com.enzoftware.translatorapp.translate.data.local

import com.enzoftware.translatorapp.core.util.CommonFlow
import com.enzoftware.translatorapp.core.util.toCommonFlow
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.history.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext

class FakeHistoryDataSource : HistoryDataSource {

    private val _data = MutableStateFlow<List<HistoryItem>>(emptyList<HistoryItem>())

    override fun getHistory(coroutineContext: CoroutineContext): CommonFlow<List<HistoryItem>> {
        return _data.toCommonFlow()
    }

    override suspend fun insertHistoryItem(item: HistoryItem) {
        _data.value += item
    }
}