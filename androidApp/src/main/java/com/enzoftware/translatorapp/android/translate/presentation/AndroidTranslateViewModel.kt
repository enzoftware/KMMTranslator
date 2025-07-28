package com.enzoftware.translatorapp.android.translate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.translate.TranslateUseCase
import com.enzoftware.translatorapp.translate.presentation.TranslateEvent
import com.enzoftware.translatorapp.translate.presentation.TranslateViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel()
class AndroidTranslateViewModel @Inject constructor(
    private val translateUseCase: TranslateUseCase,
    private val historyDataSource: HistoryDataSource,
) : ViewModel() {
    private val viewmodel by lazy {
        TranslateViewModel(
            translateUseCase = translateUseCase,
            historyDataSource = historyDataSource,
            coroutineScope = viewModelScope
        )
    }

    val state = viewmodel.state

    fun onEvent(event: TranslateEvent) {
        viewmodel.onEvent(event)
    }
}