package com.enzoftware.translatorapp.android.voice_to_text.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enzoftware.translatorapp.voice_to_text.domain.VoiceToTextParser
import com.enzoftware.translatorapp.voice_to_text.presentation.VoiceToTextEvent
import com.enzoftware.translatorapp.voice_to_text.presentation.VoiceToTextViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidVoiceToTextViewModel @Inject constructor(
    private val voiceToTextParser: VoiceToTextParser,
) : ViewModel() {

    private val viewModel by lazy {
        VoiceToTextViewModel(
            voiceToTextParser = voiceToTextParser,
            coroutineScope = viewModelScope,
        )
    }

    val state = viewModel.state

    fun onEvent(voiceToTextEvent: VoiceToTextEvent) {
        viewModel.onEvent(voiceToTextEvent)
    }

    override fun onCleared() {
        super.onCleared()
        voiceToTextParser.stopListening()
    }
}