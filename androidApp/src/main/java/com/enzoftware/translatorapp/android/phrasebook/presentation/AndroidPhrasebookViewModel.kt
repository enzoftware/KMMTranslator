package com.enzoftware.translatorapp.android.phrasebook.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enzoftware.translatorapp.phrasebook.domain.SavedWordsDataSource
import com.enzoftware.translatorapp.phrasebook.presentation.PhrasebookEvent
import com.enzoftware.translatorapp.phrasebook.presentation.PhrasebookViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidPhrasebookViewModel @Inject constructor(
    savedWordsDataSource: SavedWordsDataSource,
) : ViewModel() {
    private val viewModel = PhrasebookViewModel(savedWordsDataSource, viewModelScope)

    val state = viewModel.state

    fun onEvent(event: PhrasebookEvent) = viewModel.onEvent(event)
}
