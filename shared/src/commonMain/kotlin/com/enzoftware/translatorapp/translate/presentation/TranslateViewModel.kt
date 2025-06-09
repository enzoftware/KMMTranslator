package com.enzoftware.translatorapp.translate.presentation

import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.core.util.toCommonStateFlow
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.translate.TranslateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

class TranslateViewModel(
    private val translateUseCase: TranslateUseCase,
    private val historyDataSource: HistoryDataSource,
    private val coroutineScope: CoroutineScope?,
) {
    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(TranslateState())

    private var translatedJob: Job? = null

    val state = combine(
        _state,
        flowOf(historyDataSource.getHistory(viewModelScope.coroutineContext)),
    ){ state, history ->
        if(state.history != history){
            state.copy(
                history = history.map {
                    UiHistoryItem(
                        id = it.id ?: 0,
                        fromText = it.fromText,
                        toText = it.toText,
                        fromLanguage = UiLanguage.byCode(it.fromLanguageCode),
                        toLanguge = UiLanguage.byCode(it.toLanguageCode),
                    )
                }
            )
        } else state
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TranslateState())
        .toCommonStateFlow()

    fun onEvent(event: TranslateEvent) {
        when(event) {
            is TranslateEvent.ChooseFromLanguage -> {
                _state.update { it.copy(fromLanguage = event.language) }
            }
            is TranslateEvent.ChooseToLanguage -> {
                _state.update { it.copy(toLanguage = event.language) }
            }
            is TranslateEvent.StopChoosingLanguage -> {
                _state.update { it.copy(isChoosingFromLanguage = false, isChoosingToLanguage = false) }
            }
            is TranslateEvent.ChangeTranslationText -> TODO()
            TranslateEvent.CloseTranslation -> TODO()
            TranslateEvent.EditTranslation -> TODO()
            TranslateEvent.OnError -> TODO()
            TranslateEvent.OpenFromLanguageDropDown -> TODO()
            TranslateEvent.OpenToLanguageDropDown -> TODO()
            TranslateEvent.RecordAudio -> TODO()
            is TranslateEvent.SelectHistoryItem -> TODO()
            is TranslateEvent.SubmitVoiceResult -> TODO()
            TranslateEvent.SwapLanguages -> TODO()
            TranslateEvent.Translate -> TODO()
        }
    }
}