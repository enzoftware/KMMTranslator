package com.enzoftware.translatorapp.translate.presentation

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.translate.data.local.FakeHistoryDataSource
import com.enzoftware.translatorapp.translate.data.remote.FakeTranslateClient
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.history.HistoryItem
import com.enzoftware.translatorapp.translate.domain.translate.TranslateError
import com.enzoftware.translatorapp.translate.domain.translate.TranslateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test

class TranslateViewModelTest {

    private lateinit var viewModel: TranslateViewModel
    private lateinit var client: FakeTranslateClient
    private lateinit var historyDataSource: HistoryDataSource

    @BeforeTest
    fun setUp() {
        client = FakeTranslateClient()
        historyDataSource = FakeHistoryDataSource()
        viewModel = TranslateViewModel(
            historyDataSource = historyDataSource,
            translateUseCase = TranslateUseCase(
                historyDataSource = historyDataSource,
                client = client
            ),
            coroutineScope = CoroutineScope(Dispatchers.Default),
        )
    }

    // --- state and history ---

    @Test
    fun stateAndHistoryDataAreProperlyCombined() = runBlocking {
        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(TranslateState())

            val item = HistoryItem(
                id = 0,
                fromLanguageCode = "en",
                fromText = "Lorem ipsum",
                toLanguageCode = "de",
                toText = "to"
            )

            historyDataSource.insertHistoryItem(item)

            val state = awaitItem()

            val expectedItem = UiHistoryItem(
                id = item.id,
                fromText = item.fromText,
                toText = item.toText,
                fromLanguage = UiLanguage.byCode(item.fromLanguageCode),
                toLanguage = UiLanguage.byCode(item.toLanguageCode),
            )

            assertThat(state.history.first()).isEqualTo(expected = expectedItem)
        }
    }

    // --- translate ---

    @Test
    fun translateSuccess_stateProperlyUpdated() = runBlocking {
        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ChangeTranslationText("text"))
            awaitItem()
            viewModel.onEvent(TranslateEvent.Translate)

            val loadingState = awaitItem()
            assertThat(loadingState.isTranslating).isTrue()

            val resultState = awaitSettledTranslation()
            assertThat(resultState.isTranslating).isFalse()
            assertThat(resultState.toText).isEqualTo("test translation")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun translateWithBlankText_doesNotTriggerTranslation() = runBlocking {
        viewModel.state.test {
            awaitItem()

            viewModel.onEvent(TranslateEvent.Translate)

            expectNoEvents()
        }
    }

    @Test
    fun translateError_errorIsSetInStateAndIsTranslatingBecomesFalse() = runBlocking {
        client.shouldReturnError = true
        client.translateError = TranslateError.SERVICE_UNAVAILABLE

        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ChangeTranslationText("some text"))
            awaitItem()
            viewModel.onEvent(TranslateEvent.Translate)

            val loadingState = awaitItem()
            assertThat(loadingState.isTranslating).isTrue()

            val errorState = awaitItem()
            assertThat(errorState.isTranslating).isFalse()
            assertThat(errorState.error).isEqualTo(TranslateError.SERVICE_UNAVAILABLE)
        }
    }

    // --- onChangeTranslationText ---

    @Test
    fun changeTranslationText_updatesFromTextInState() = runBlocking {
        viewModel.state.test {
            awaitItem()

            viewModel.onEvent(TranslateEvent.ChangeTranslationText("hello world"))

            val state = awaitItem()
            assertThat(state.fromText).isEqualTo("hello world")
        }
    }

    // --- onOpenFromLanguageDropDown / onOpenToLanguageDropDown ---

    @Test
    fun openFromLanguageDropDown_setsIsChoosingFromLanguageToTrue() = runBlocking {
        viewModel.state.test {
            awaitItem()

            viewModel.onEvent(TranslateEvent.OpenFromLanguageDropDown)

            val state = awaitItem()
            assertThat(state.isChoosingFromLanguage).isTrue()
        }
    }

    @Test
    fun openToLanguageDropDown_setsIsChoosingToLanguageToTrue() = runBlocking {
        viewModel.state.test {
            awaitItem()

            viewModel.onEvent(TranslateEvent.OpenToLanguageDropDown)

            val state = awaitItem()
            assertThat(state.isChoosingToLanguage).isTrue()
        }
    }

    // --- onChooseFromLanguage / onChooseToLanguage ---

    @Test
    fun chooseFromLanguage_updatesFromLanguageAndClosesDropdown() = runBlocking {
        val german = UiLanguage.byCode("de")

        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.OpenFromLanguageDropDown)
            awaitItem()

            viewModel.onEvent(TranslateEvent.ChooseFromLanguage(german))

            val state = awaitItem()
            assertThat(state.fromLanguage).isEqualTo(german)
            assertThat(state.isChoosingFromLanguage).isFalse()
        }
    }

    @Test
    fun chooseToLanguage_updatesToLanguageAndClosesDropdown() = runBlocking {
        val french = UiLanguage.byCode("fr")

        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.OpenToLanguageDropDown)
            awaitItem()

            viewModel.onEvent(TranslateEvent.ChooseToLanguage(french))

            val state = awaitItem()
            assertThat(state.toLanguage).isEqualTo(french)
            assertThat(state.isChoosingToLanguage).isFalse()
        }
    }

    // --- onStopChoosingLanguage ---

    @Test
    fun stopChoosingLanguage_closesBothLanguageDropdowns() = runBlocking {
        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.OpenFromLanguageDropDown)
            awaitItem()

            viewModel.onEvent(TranslateEvent.StopChoosingLanguage)

            val state = awaitItem()
            assertThat(state.isChoosingFromLanguage).isFalse()
            assertThat(state.isChoosingToLanguage).isFalse()
        }
    }

    // --- onSwapLanguages ---

    @Test
    fun swapLanguages_swapsFromLanguageAndToLanguage() = runBlocking {
        viewModel.state.test {
            val initialState = awaitItem()
            val originalFrom = initialState.fromLanguage
            val originalTo = initialState.toLanguage

            viewModel.onEvent(TranslateEvent.SwapLanguages)

            val state = awaitItem()
            assertThat(state.fromLanguage).isEqualTo(originalTo)
            assertThat(state.toLanguage).isEqualTo(originalFrom)
        }
    }

    @Test
    fun swapLanguagesAfterTranslation_alsoSwapsFromTextAndToText() = runBlocking {
        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ChangeTranslationText("hello"))
            awaitItem()
            viewModel.onEvent(TranslateEvent.Translate)
            val settled = awaitSettledTranslation()
            val originalFrom = settled.fromLanguage

            viewModel.onEvent(TranslateEvent.SwapLanguages)

            var state = awaitItem()
            while (state.toLanguage != originalFrom) {
                state = awaitItem()
            }
            assertThat(state.fromText).isEqualTo("test translation")
            assertThat(state.toText).isEqualTo("hello")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- onCloseTranslation ---

    @Test
    fun closeTranslation_resetsFromTextAndToTextAndIsTranslating() = runBlocking {
        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ChangeTranslationText("hello"))
            awaitItem()
            viewModel.onEvent(TranslateEvent.Translate)
            awaitSettledTranslation()

            viewModel.onEvent(TranslateEvent.CloseTranslation)

            val state = awaitItem()
            assertThat(state.fromText).isEqualTo("")
            assertThat(state.toText).isNull()
            assertThat(state.isTranslating).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- onEditTranslation ---

    @Test
    fun editTranslationWithToText_clearsToTextAndStopsTranslating() = runBlocking {
        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ChangeTranslationText("hello"))
            awaitItem()
            viewModel.onEvent(TranslateEvent.Translate)
            val settled = awaitSettledTranslation()

            viewModel.onEvent(TranslateEvent.EditTranslation)

            val state = awaitItem()
            assertThat(state.toText).isNull()
            assertThat(state.isTranslating).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun editTranslationWithoutToText_stateRemainsUnchanged() = runBlocking {
        viewModel.state.test {
            awaitItem()
            assertThat(viewModel.state.value.toText).isNull()

            viewModel.onEvent(TranslateEvent.EditTranslation)

            expectNoEvents()
        }
    }

    // --- onSelectHistoryItem ---

    @Test
    fun selectHistoryItem_restoresStateFromTheSelectedHistoryItem() = runBlocking {
        val historyItem = UiHistoryItem(
            id = 1L,
            fromText = "hello",
            toText = "hola",
            fromLanguage = UiLanguage.byCode("en"),
            toLanguage = UiLanguage.byCode("es"),
        )

        viewModel.state.test {
            awaitItem()

            viewModel.onEvent(TranslateEvent.SelectHistoryItem(historyItem))

            val state = awaitItem()
            assertThat(state.fromText).isEqualTo(historyItem.fromText)
            assertThat(state.toText).isEqualTo(historyItem.toText)
            assertThat(state.fromLanguage).isEqualTo(historyItem.fromLanguage)
            assertThat(state.toLanguage).isEqualTo(historyItem.toLanguage)
            assertThat(state.isTranslating).isFalse()
        }
    }

    // --- onSubmitVoiceResult ---

    @Test
    fun submitVoiceResultWithText_updatesFromTextAndClearsToText() = runBlocking {
        viewModel.state.test {
            awaitItem()

            viewModel.onEvent(TranslateEvent.SubmitVoiceResult("voice text"))

            val state = awaitItem()
            assertThat(state.fromText).isEqualTo("voice text")
            assertThat(state.toText).isNull()
            assertThat(state.isTranslating).isFalse()
        }
    }

    @Test
    fun submitVoiceResultWithNull_stateRemainsUnchanged() = runBlocking {
        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ChangeTranslationText("existing text"))
            awaitItem()

            viewModel.onEvent(TranslateEvent.SubmitVoiceResult(null))

            expectNoEvents()
        }
    }

    // --- onError ---

    @Test
    fun onError_clearsErrorFieldInState() = runBlocking {
        client.shouldReturnError = true
        client.translateError = TranslateError.SERVER_ERROR

        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ChangeTranslationText("text"))
            awaitItem()
            viewModel.onEvent(TranslateEvent.Translate)
            awaitItem() // loading
            val errorState = awaitItem()
            assertThat(errorState.error).isNotNull()

            viewModel.onEvent(TranslateEvent.OnError)

            val clearedState = awaitItem()
            assertThat(clearedState.error).isNull()
            assertThat(clearedState.isTranslating).isFalse()
        }
    }

    // --- phrasebook ---

    @Test
    fun showPhrasebookEntry_showsTheBuiltInTranslationOfflineWithoutHistory() = runBlocking {
        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ShowPhrasebookEntry("good_morning"))

            val state = awaitItem()
            assertThat(state.fromText).isEqualTo("Good morning")
            assertThat(state.toText).isEqualTo("Buenos días")
            assertThat(state.isTranslating).isFalse()
            assertThat(state.history.isEmpty()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun showPhrasebookEntry_withASavedPair_switchesToThatPair() = runBlocking {
        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ShowPhrasebookEntry("thank_you", "fr", "ja"))

            val state = awaitItem()
            assertThat(state.fromLanguage).isEqualTo(UiLanguage.byCode("fr"))
            assertThat(state.toLanguage).isEqualTo(UiLanguage.byCode("ja"))
            assertThat(state.fromText).isEqualTo("Merci")
            assertThat(state.toText).isEqualTo("ありがとうございます")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun showPhrasebookEntry_withUnknownKey_changesNothing() = runBlocking {
        viewModel.onEvent(TranslateEvent.ShowPhrasebookEntry("not_a_phrase"))
        assertThat(viewModel.state.value).isEqualTo(TranslateState())
    }

    /**
     * A successful translation emits twice, once for the result and once for the history insert,
     * in no guaranteed order. Waits until both have landed.
     */
    private suspend fun ReceiveTurbine<TranslateState>.awaitSettledTranslation(): TranslateState {
        var state = awaitItem()
        while (state.toText == null || state.history.isEmpty()) {
            state = awaitItem()
        }
        return state
    }
}
