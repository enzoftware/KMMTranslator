package com.enzoftware.translatorapp.translate.presentation

import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.core.util.Resource
import com.enzoftware.translatorapp.core.util.toCommonStateFlow
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.translate.TranslateException
import com.enzoftware.translatorapp.translate.domain.translate.TranslateUseCase
import com.enzoftware.translatorapp.translate.presentation.TranslateEvent.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TranslateViewModel(
    private val translateUseCase: TranslateUseCase,
    private val historyDataSource: HistoryDataSource,
    private val coroutineScope: CoroutineScope?,
) {
    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(TranslateState())

    private var translateJob: Job? = null

    val state = combine(
        _state,
        historyDataSource.getHistory(viewModelScope.coroutineContext),
    ) { state, history ->
        if (state.history != history) {
            state.copy(
                history = history.map {
                    UiHistoryItem(
                        id = it.id ?: -1,
                        fromText = it.fromText,
                        toText = it.toText,
                        fromLanguage = UiLanguage.byCode(it.fromLanguageCode),
                        toLanguage = UiLanguage.byCode(it.toLanguageCode),
                    )
                }
            )
        } else state
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        TranslateState()
    ).toCommonStateFlow()

    private fun translate(state: TranslateState) {
        if (state.fromText.isBlank() || state.isTranslating) {
            return
        }
        translateJob = viewModelScope.launch {
            _state.update {
                it.copy(isTranslating = true)
            }
            val result = translateUseCase.execute(
                fromLanguage = state.fromLanguage.language,
                fromText = state.fromText,
                toLanguage = state.toLanguage.language,
            )
            when (result) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isTranslating = false,
                            toText = result.data,
                        )
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isTranslating = false,
                            error = (result.throwable as? TranslateException)?.error
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: TranslateEvent) {
        when (event) {
            is ChooseFromLanguage -> onChooseFromLanguage(event)
            is ChooseToLanguage -> onChooseToLanguage(event)
            is StopChoosingLanguage -> onStopChoosingLanguage()
            is CloseTranslation -> onCloseTranslation()
            is EditTranslation -> onEditTranslation()
            is ChangeTranslationText -> onChangeTranslationText(event.text)
            is OnError -> onError()
            OpenFromLanguageDropDown -> onOpenFromLanguageDropDown()
            OpenToLanguageDropDown -> onOpenToLanguageDropDown()
            is SelectHistoryItem -> onSelectHistoryItem(event.item)
            is SubmitVoiceResult -> onSubmitVoiceResult(event)
            SwapLanguages -> onSwapLanguages()
            Translate -> translate(_state.value)
            else -> Unit
        }
    }

    /**
     * Handles the event of selecting a history item for translation.
     * This updates the state with the selected item's details and triggers a new translation.
     *
     * @param item The selected history item.
     */
    private fun onSelectHistoryItem(item: UiHistoryItem) {
        translateJob?.cancel()
        _state.update {
            it.copy(
                fromText = item.fromText,
                toText = item.toText,
                isTranslating = false,
                fromLanguage = item.fromLanguage,
                toLanguage = item.toLanguage
            )
        }
    }


    /**
     * Handles the event of changing the translation text.
     * This updates the state with the new "fromText" and resets the "toText" if necessary.
     *
     * @param text The new text to translate.
     */
    private fun onChangeTranslationText(text: String) {
        _state.update {
            it.copy(
                fromText = text
            )
        }
    }


    /**
     * Handles the event of submitting the voice result for translation.
     * This updates the state with the voice result and triggers a new translation.
     *
     * @param event The event containing the voice result.
     */
    private fun onSubmitVoiceResult(event: SubmitVoiceResult) {
        _state.update {
            it.copy(
                fromText = event.result ?: it.fromText,
                isTranslating = if (event.result != null) false else it.isTranslating,
                toText = if (event.result != null) null else it.toText
            )
        }
    }

    /**
     * Handles the event of editing the translation.
     * This updates the state to allow the user to edit the "to" text.
     * If there is no "toText", it resets the translation state.
     */
    private fun onEditTranslation() {
        if (_state.value.toText != null) {
            _state.update {
                it.copy(
                    toText = null,
                    isTranslating = false,
                )
            }
        }
    }

    /**
     * Handles the event of opening the dropdown for selecting the "from" language.
     * This updates the state to indicate that the "from" language selection is being made.
     */
    private fun onOpenFromLanguageDropDown() {
        _state.update {
            it.copy(
                isChoosingFromLanguage = true
            )
        }
    }

    /**
     * Handles the event of opening the dropdown for selecting the "to" language.
     * This updates the state to indicate that the "to" language selection is being made.
     */
    private fun onOpenToLanguageDropDown() {
        _state.update {
            it.copy(
                isChoosingToLanguage = true
            )
        }
    }

    /**
     * Handles the event of an error occurring during translation.
     * This updates the state with the error message and resets the translation state.
     */
    private fun onError() {
        _state.update {
            it.copy(
                isTranslating = false,
                error = null,
            )
        }
    }


    /**
     * Handles the event of swapping the languages for translation.
     * This updates the state with the swapped languages and triggers a new translation.
     *
     */
    private fun onSwapLanguages() {
        _state.update {
            it.copy(
                fromLanguage = it.toLanguage,
                toLanguage = it.fromLanguage,
                fromText = it.toText ?: "",
                toText = if (it.toText != null) it.fromText else null,
                isTranslating = false,
            )
        }
    }

    /**
     * Handles the event of choosing a language for translation.
     * This updates the state with the selected language
     *
     * @param event The event containing the selected language.
     */
    private fun onChooseFromLanguage(event: ChooseFromLanguage) {
        _state.update { it.copy(isChoosingFromLanguage = false, fromLanguage = event.language) }
    }

    /**
     * Handles the event of choosing a target language for translation.
     * This updates the state with the selected language and triggers a new translation.
     *
     * @param event The event containing the selected language.
     */
    private fun onChooseToLanguage(event: ChooseToLanguage) {
        val state = _state.updateAndGet { it.copy(toLanguage = event.language, isChoosingToLanguage = false) }
        translate(state)
    }

    /**
     * Handles the event of stopping the language selection process.
     * This updates the state to indicate that no language is being chosen.
     */
    private fun onStopChoosingLanguage() {
        _state.update { it.copy(isChoosingFromLanguage = false, isChoosingToLanguage = false) }
    }

    /**
     * Handles the event of closing the translation view.
     * This resets the translation state and clears the "toText".
     */
    private fun onCloseTranslation() {
        _state.update {
            it.copy(
                isTranslating = false,
                toText = null,
                fromText = "",
            )
        }
    }
}