package com.enzoftware.translatorapp.phrasebook

import com.enzoftware.translatorapp.core.util.CommonFlow
import com.enzoftware.translatorapp.core.util.toCommonFlow
import com.enzoftware.translatorapp.phrasebook.domain.SavedWord
import com.enzoftware.translatorapp.phrasebook.domain.SavedWordsDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSavedWordsDataSource : SavedWordsDataSource {
    val words = MutableStateFlow<List<SavedWord>>(emptyList())

    override fun observeSavedWords(): CommonFlow<List<SavedWord>> = words.toCommonFlow()

    override suspend fun save(word: SavedWord) = words.update { list ->
        (listOf(word) + list.filterNot { it.sameAs(word.entryKey, word.fromLanguageCode, word.toLanguageCode) })
            .sortedByDescending { it.savedAt }
    }

    override suspend fun remove(entryKey: String, fromLanguageCode: String, toLanguageCode: String) =
        words.update { list -> list.filterNot { it.sameAs(entryKey, fromLanguageCode, toLanguageCode) } }

    private fun SavedWord.sameAs(key: String, from: String, to: String) =
        entryKey == key && fromLanguageCode == from && toLanguageCode == to
}
