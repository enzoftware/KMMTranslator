package com.enzoftware.translatorapp.phrasebook

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.phrasebook.domain.PhrasebookCategory
import com.enzoftware.translatorapp.phrasebook.presentation.PhrasebookEvent
import com.enzoftware.translatorapp.phrasebook.presentation.PhrasebookState
import com.enzoftware.translatorapp.phrasebook.presentation.PhrasebookViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test

class PhrasebookViewModelTest {

    private val dataSource = FakeSavedWordsDataSource()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var clock = 100L
    private val viewModel = PhrasebookViewModel(dataSource, scope, now = { clock++ })

    private val english = UiLanguage.byCode("en")
    private val german = UiLanguage.byCode("de")
    private val japanese = UiLanguage.byCode("ja")

    @AfterTest
    fun tearDown() = scope.cancel()

    /** Skips intermediate emissions until [predicate] holds. */
    private suspend fun ReceiveTurbine<PhrasebookState>.awaitState(predicate: (PhrasebookState) -> Boolean): PhrasebookState {
        var state = awaitItem()
        while (!predicate(state)) state = awaitItem()
        return state
    }

    @Test
    fun suggestionsShowEveryPhraseInTheSelectedPair() = runBlocking {
        viewModel.state.test {
            viewModel.onEvent(PhrasebookEvent.SetLanguages(english, german))
            val state = awaitState { it.toLanguage == german }

            val thanks = state.suggestions.first { it.entryKey == "thank_you" }
            assertThat(thanks.fromText).isEqualTo("Thank you")
            assertThat(thanks.toText).isEqualTo("Danke")
            assertThat(thanks.isSaved).isFalse()
            assertThat(state.suggestions.size).isEqualTo(25)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun categoryFilterNarrowsTheSuggestions() = runBlocking {
        viewModel.state.test {
            viewModel.onEvent(PhrasebookEvent.SelectCategory(PhrasebookCategory.FOOD))
            val food = awaitState { it.selectedCategory == PhrasebookCategory.FOOD }
            assertThat(food.suggestions.map { it.category }.toSet()).isEqualTo(setOf(PhrasebookCategory.FOOD))

            viewModel.onEvent(PhrasebookEvent.SelectCategory(null))
            assertThat(awaitState { it.selectedCategory == null }.suggestions.size).isEqualTo(25)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun togglingSavesForThePairThenRemoves() = runBlocking {
        viewModel.state.test {
            viewModel.onEvent(PhrasebookEvent.SetLanguages(english, german))
            awaitState { it.toLanguage == german }

            viewModel.onEvent(PhrasebookEvent.ToggleSaved("thank_you"))
            val saved = awaitState { it.savedWords.isNotEmpty() }
            assertThat(saved.suggestions.first { it.entryKey == "thank_you" }.isSaved).isTrue()
            assertThat(saved.savedWords.single().let { Triple(it.fromText, it.toText, it.toLanguage) })
                .isEqualTo(Triple("Thank you", "Danke", german))

            viewModel.onEvent(PhrasebookEvent.ToggleSaved("thank_you"))
            val removed = awaitState { it.savedWords.isEmpty() }
            assertThat(removed.suggestions.first { it.entryKey == "thank_you" }.isSaved).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun savedStateIsPerLanguagePair() = runBlocking {
        viewModel.state.test {
            viewModel.onEvent(PhrasebookEvent.SetLanguages(english, german))
            awaitState { it.toLanguage == german }
            viewModel.onEvent(PhrasebookEvent.ToggleSaved("water"))
            awaitState { it.savedWords.size == 1 }

            viewModel.onEvent(PhrasebookEvent.SetLanguages(english, japanese))
            val otherPair = awaitState { it.toLanguage == japanese }
            assertThat(otherPair.suggestions.first { it.entryKey == "water" }.isSaved).isFalse()
            // My Words still lists the German save.
            assertThat(otherPair.savedWords.map { it.toText }).containsExactly("Wasser")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removeSavedWordDeletesThatPairOnly() = runBlocking {
        viewModel.state.test {
            viewModel.onEvent(PhrasebookEvent.SetLanguages(english, german))
            awaitState { it.toLanguage == german }
            viewModel.onEvent(PhrasebookEvent.ToggleSaved("hello"))
            awaitState { it.savedWords.size == 1 }
            viewModel.onEvent(PhrasebookEvent.SetLanguages(english, japanese))
            awaitState { it.toLanguage == japanese }
            viewModel.onEvent(PhrasebookEvent.ToggleSaved("hello"))
            awaitState { it.savedWords.size == 2 }

            viewModel.onEvent(PhrasebookEvent.RemoveSavedWord("hello", "en", "de"))
            val state = awaitState { it.savedWords.size == 1 }
            assertThat(state.savedWords.single().toLanguage).isEqualTo(japanese)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(dataSource.words.value.map { it.toLanguageCode }).containsExactly("ja")
    }

    @Test
    fun unknownEntriesAreIgnored() = runBlocking {
        viewModel.onEvent(PhrasebookEvent.ToggleSaved("not_a_phrase"))
        assertThat(dataSource.words.value).isEmpty()
    }
}
