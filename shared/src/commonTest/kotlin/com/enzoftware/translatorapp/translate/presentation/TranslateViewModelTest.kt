package com.enzoftware.translatorapp.translate.presentation

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
    fun `state and history data are properly combined`() = runBlocking {
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
    fun `translate success - state properly updated`() = runBlocking {
        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ChangeTranslationText("text"))
            awaitItem()
            viewModel.onEvent(TranslateEvent.Translate)

            val loadingState = awaitItem()
            assertThat(loadingState.isTranslating).isTrue()

            val resultState = awaitItem()
            assertThat(resultState.isTranslating).isFalse()
            assertThat(resultState.toText).isEqualTo("test translation")
        }
    }

    @Test
    fun `translate with blank text - does not trigger translation`() = runBlocking {
        viewModel.state.test {
            awaitItem()

            viewModel.onEvent(TranslateEvent.Translate)

            expectNoEvents()
        }
    }

    @Test
    fun `translate error - error is set in state and isTranslating becomes false`() = runBlocking {
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
    fun `ChangeTranslationText - updates fromText in state`() = runBlocking {
        viewModel.state.test {
            awaitItem()

            viewModel.onEvent(TranslateEvent.ChangeTranslationText("hello world"))

            val state = awaitItem()
            assertThat(state.fromText).isEqualTo("hello world")
        }
    }

    // --- onOpenFromLanguageDropDown / onOpenToLanguageDropDown ---

    @Test
    fun `OpenFromLanguageDropDown - sets isChoosingFromLanguage to true`() = runBlocking {
        viewModel.state.test {
            awaitItem()

            viewModel.onEvent(TranslateEvent.OpenFromLanguageDropDown)

            val state = awaitItem()
            assertThat(state.isChoosingFromLanguage).isTrue()
        }
    }

    @Test
    fun `OpenToLanguageDropDown - sets isChoosingToLanguage to true`() = runBlocking {
        viewModel.state.test {
            awaitItem()

            viewModel.onEvent(TranslateEvent.OpenToLanguageDropDown)

            val state = awaitItem()
            assertThat(state.isChoosingToLanguage).isTrue()
        }
    }

    // --- onChooseFromLanguage / onChooseToLanguage ---

    @Test
    fun `ChooseFromLanguage - updates fromLanguage and closes dropdown`() = runBlocking {
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
    fun `ChooseToLanguage - updates toLanguage and closes dropdown`() = runBlocking {
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
    fun `StopChoosingLanguage - closes both language dropdowns`() = runBlocking {
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
    fun `SwapLanguages - swaps fromLanguage and toLanguage`() = runBlocking {
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
    fun `SwapLanguages after translation - also swaps fromText and toText`() = runBlocking {
        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ChangeTranslationText("hello"))
            awaitItem()
            viewModel.onEvent(TranslateEvent.Translate)
            awaitItem() // loading
            awaitItem() // result: toText = "test translation"

            viewModel.onEvent(TranslateEvent.SwapLanguages)

            val state = awaitItem()
            assertThat(state.fromText).isEqualTo("test translation")
            assertThat(state.toText).isEqualTo("hello")
        }
    }

    // --- onCloseTranslation ---

    @Test
    fun `CloseTranslation - resets fromText, toText, and isTranslating`() = runBlocking {
        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ChangeTranslationText("hello"))
            awaitItem()
            viewModel.onEvent(TranslateEvent.Translate)
            awaitItem() // loading
            awaitItem() // result

            viewModel.onEvent(TranslateEvent.CloseTranslation)

            val state = awaitItem()
            assertThat(state.fromText).isEqualTo("")
            assertThat(state.toText).isNull()
            assertThat(state.isTranslating).isFalse()
        }
    }

    // --- onEditTranslation ---

    @Test
    fun `EditTranslation with toText - clears toText and stops translating`() = runBlocking {
        viewModel.state.test {
            awaitItem()
            viewModel.onEvent(TranslateEvent.ChangeTranslationText("hello"))
            awaitItem()
            viewModel.onEvent(TranslateEvent.Translate)
            awaitItem() // loading
            awaitItem() // result: toText = "test translation"

            viewModel.onEvent(TranslateEvent.EditTranslation)

            val state = awaitItem()
            assertThat(state.toText).isNull()
            assertThat(state.isTranslating).isFalse()
        }
    }

    @Test
    fun `EditTranslation without toText - state remains unchanged`() = runBlocking {
        viewModel.state.test {
            awaitItem()
            assertThat(viewModel.state.value.toText).isNull()

            viewModel.onEvent(TranslateEvent.EditTranslation)

            expectNoEvents()
        }
    }

    // --- onSelectHistoryItem ---

    @Test
    fun `SelectHistoryItem - restores state from the selected history item`() = runBlocking {
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
    fun `SubmitVoiceResult with text - updates fromText and clears toText`() = runBlocking {
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
    fun `SubmitVoiceResult with null - state remains unchanged`() = runBlocking {
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
    fun `OnError - clears error field in state`() = runBlocking {
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
}
