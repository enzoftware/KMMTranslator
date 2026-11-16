package com.enzoftware.translatorapp.phrasebook.data

import com.enzoftware.translatorapp.core.util.CommonFlow
import com.enzoftware.translatorapp.core.util.toCommonFlow
import com.enzoftware.translatorapp.phrasebook.domain.SavedWord
import com.enzoftware.translatorapp.phrasebook.domain.SavedWordsDataSource
import com.enzoftware.translatorapp.translate.data.local.SavedWordDao
import com.enzoftware.translatorapp.translate.data.local.SavedWordEntity
import kotlinx.coroutines.flow.map

class RoomSavedWordsDataSource(private val dao: SavedWordDao) : SavedWordsDataSource {

    override fun observeSavedWords(): CommonFlow<List<SavedWord>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }.toCommonFlow()

    override suspend fun save(word: SavedWord) {
        dao.upsert(
            SavedWordEntity(
                entryKey = word.entryKey,
                fromLanguageCode = word.fromLanguageCode,
                fromText = word.fromText,
                toLanguageCode = word.toLanguageCode,
                toText = word.toText,
                savedAt = word.savedAt,
            ),
        )
    }

    override suspend fun remove(entryKey: String, fromLanguageCode: String, toLanguageCode: String) {
        dao.delete(entryKey, fromLanguageCode, toLanguageCode)
    }
}

private fun SavedWordEntity.toDomain() = SavedWord(
    entryKey = entryKey,
    fromLanguageCode = fromLanguageCode,
    fromText = fromText,
    toLanguageCode = toLanguageCode,
    toText = toText,
    savedAt = savedAt,
)
