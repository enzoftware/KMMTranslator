package com.enzoftware.translatorapp.phrasebook.domain

import com.enzoftware.translatorapp.core.util.CommonFlow

/**
 * A phrasebook entry the user saved for a language pair ("My Words"). The texts are a snapshot, so
 * the list stays readable (and indexable for search) even if the phrasebook copy changes later.
 */
data class SavedWord(
    val entryKey: String,
    val fromLanguageCode: String,
    val fromText: String,
    val toLanguageCode: String,
    val toText: String,
    val savedAt: Long,
)

interface SavedWordsDataSource {
    /** Newest first. */
    fun observeSavedWords(): CommonFlow<List<SavedWord>>
    suspend fun save(word: SavedWord)
    suspend fun remove(entryKey: String, fromLanguageCode: String, toLanguageCode: String)
}
