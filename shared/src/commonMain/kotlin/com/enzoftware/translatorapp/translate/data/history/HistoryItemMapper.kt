package com.enzoftware.translatorapp.translate.data.history

import com.enzoftware.translatorapp.translate.domain.history.HistoryItem
import database.HistoryEntity

fun HistoryEntity.toHistoryItem(): HistoryItem {
    return HistoryItem(
        id = id,
        fromLanguageCode = fromLanguageCode,
        fromText = fromText,
        toLanguageCode = toLanguage,
        toText = toText,
    )
}