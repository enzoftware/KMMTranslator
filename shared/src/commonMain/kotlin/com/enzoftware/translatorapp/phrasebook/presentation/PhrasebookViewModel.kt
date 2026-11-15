package com.enzoftware.translatorapp.phrasebook.presentation

import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.core.util.toCommonStateFlow
import com.enzoftware.translatorapp.phrasebook.domain.Phrasebook
import com.enzoftware.translatorapp.phrasebook.domain.PhrasebookCategory
import com.enzoftware.translatorapp.phrasebook.domain.SavedWord
import com.enzoftware.translatorapp.phrasebook.domain.SavedWordsDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class UiPhrase(
    val entryKey: String,
    val category: PhrasebookCategory,
    val fromText: String,
    val toText: String,
    val isSaved: Boolean,
)

data class UiSavedWord(
    val entryKey: String,
    val fromLanguage: UiLanguage,
    val toLanguage: UiLanguage,
    val fromText: String,
    val toText: String,
)

data class PhrasebookState(
    val fromLanguage: UiLanguage = UiLanguage.byCode("en"),
    val toLanguage: UiLanguage = UiLanguage.byCode("es"),
    /** null shows every category. */
    val selectedCategory: PhrasebookCategory? = null,
    val suggestions: List<UiPhrase> = emptyList(),
    /** Every saved word, across all language pairs, newest first. */
    val savedWords: List<UiSavedWord> = emptyList(),
)

sealed class PhrasebookEvent {
    data class SetLanguages(val fromLanguage: UiLanguage, val toLanguage: UiLanguage) : PhrasebookEvent()
    data class SelectCategory(val category: PhrasebookCategory?) : PhrasebookEvent()

    /** Saves the entry for the current language pair, or removes it if already saved. */
    data class ToggleSaved(val entryKey: String) : PhrasebookEvent()
    data class RemoveSavedWord(
        val entryKey: String,
        val fromLanguageCode: String,
        val toLanguageCode: String,
    ) : PhrasebookEvent()
}

private data class Selection(
    val fromLanguage: UiLanguage,
    val toLanguage: UiLanguage,
    val category: PhrasebookCategory?,
)

class PhrasebookViewModel(
    private val savedWordsDataSource: SavedWordsDataSource,
    coroutineScope: CoroutineScope?,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)

    private val selection = MutableStateFlow(
        Selection(UiLanguage.byCode("en"), UiLanguage.byCode("es"), category = null),
    )
    private val savedWords = MutableStateFlow<List<SavedWord>>(emptyList())

    init {
        viewModelScope.launch {
            savedWordsDataSource.observeSavedWords().collect { savedWords.value = it }
        }
    }

    val state = combine(selection, savedWords) { selection, saved -> buildState(selection, saved) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PhrasebookState())
        .toCommonStateFlow()

    fun onEvent(event: PhrasebookEvent) {
        when (event) {
            is PhrasebookEvent.SetLanguages -> selection.update {
                it.copy(fromLanguage = event.fromLanguage, toLanguage = event.toLanguage)
            }

            is PhrasebookEvent.SelectCategory -> selection.update { it.copy(category = event.category) }
            is PhrasebookEvent.ToggleSaved -> toggleSaved(event.entryKey)
            is PhrasebookEvent.RemoveSavedWord -> viewModelScope.launch {
                savedWordsDataSource.remove(event.entryKey, event.fromLanguageCode, event.toLanguageCode)
            }
        }
    }

    private fun toggleSaved(entryKey: String) {
        val entry = Phrasebook.byKey(entryKey) ?: return
        val (from, to) = selection.value.let { it.fromLanguage.language to it.toLanguage.language }
        val alreadySaved = savedWords.value.any { it.matches(entryKey, from.code, to.code) }
        viewModelScope.launch {
            if (alreadySaved) {
                savedWordsDataSource.remove(entryKey, from.code, to.code)
            } else {
                savedWordsDataSource.save(
                    SavedWord(
                        entryKey = entryKey,
                        fromLanguageCode = from.code,
                        fromText = entry.textIn(from),
                        toLanguageCode = to.code,
                        toText = entry.textIn(to),
                        savedAt = now(),
                    ),
                )
            }
        }
    }

    private fun buildState(selection: Selection, saved: List<SavedWord>): PhrasebookState {
        val from = selection.fromLanguage.language
        val to = selection.toLanguage.language
        return PhrasebookState(
            fromLanguage = selection.fromLanguage,
            toLanguage = selection.toLanguage,
            selectedCategory = selection.category,
            suggestions = Phrasebook.entries
                .filter { selection.category == null || it.category == selection.category }
                .map { entry ->
                    UiPhrase(
                        entryKey = entry.key,
                        category = entry.category,
                        fromText = entry.textIn(from),
                        toText = entry.textIn(to),
                        isSaved = saved.any { it.matches(entry.key, from.code, to.code) },
                    )
                },
            savedWords = saved.map { word ->
                UiSavedWord(
                    entryKey = word.entryKey,
                    fromLanguage = UiLanguage.byCode(word.fromLanguageCode),
                    toLanguage = UiLanguage.byCode(word.toLanguageCode),
                    fromText = word.fromText,
                    toText = word.toText,
                )
            },
        )
    }
}

private fun SavedWord.matches(key: String, fromCode: String, toCode: String) =
    entryKey == key && fromLanguageCode == fromCode && toLanguageCode == toCode
