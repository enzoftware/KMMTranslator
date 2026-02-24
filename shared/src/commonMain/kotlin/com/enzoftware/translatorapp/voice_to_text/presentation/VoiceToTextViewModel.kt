package com.enzoftware.translatorapp.voice_to_text.presentation

import com.enzoftware.translatorapp.core.util.toCommonStateFlow
import com.enzoftware.translatorapp.voice_to_text.domain.VoiceToTextParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class VoiceToTextViewModel(
    private val voiceToTextParser: VoiceToTextParser,
    coroutineScope: CoroutineScope? = null,
) {
    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(VoiceToTextState())

    val state = _state.combine(voiceToTextParser.state) { state, voiceResult ->
        state.copy(
            spokenText = voiceResult.result,
            recordErrorText = if (state.canRecord) {
                voiceResult.error
            } else {
                "Can't record without permission"
            },
            displayState = when {
                !state.canRecord || voiceResult.error != null -> DisplayState.ERROR
                voiceResult.result.isNotBlank() && !voiceResult.isSpeaking -> {
                    DisplayState.DISPLAYING_RESULTS
                }

                voiceResult.isSpeaking -> DisplayState.SPEAKING
                else -> DisplayState.WAITING_TO_TALK
            }
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VoiceToTextState())
        .toCommonStateFlow()

    init {
        viewModelScope.launch {
//            while (true) {
//                if (state.value.displayState == DisplayState.SPEAKING) {
//                    _state.update {
//                        it.copy(
//                            powerRatios = it.powerRatios + voiceToTextParser.state.value.powerRatio
//                        )
//                    }
//                }
//                delay(50L)
//            }
            voiceToTextParser.state
                .filter { it.isSpeaking }
                .sample(50L)
                .collect { voiceResult ->
                    _state.update { currentState ->
                        currentState.copy(
                            powerRatios = currentState.powerRatios + voiceResult.powerRatio
                        )
                    }
                }
        }
    }

    fun onEvent(event: VoiceToTextEvent) {
        when (event) {
            VoiceToTextEvent.Close -> onCloseVoiceToText()
            is VoiceToTextEvent.PermissionResult -> onPermissionResult(event.isGranted, event.isPermanentDeclined)
            VoiceToTextEvent.Reset -> onReset()
            is VoiceToTextEvent.ToggleRecording -> onToggleRecording(event.languageCode)
        }
    }

    private fun onToggleRecording(language: String) {
        val wasSpeaking = state.value.displayState == DisplayState.SPEAKING

        _state.update {
            it.copy(
                powerRatios = emptyList(),
            )
        }

        if (wasSpeaking) {
            voiceToTextParser.stopListening()
        } else {
            voiceToTextParser.cancel()
            voiceToTextParser.startListening(language)
        }
    }

    private fun onCloseVoiceToText() = Unit

    private fun onPermissionResult(isGranted: Boolean, isPermanentDenied: Boolean) {
        _state.update {
            it.copy(
                canRecord = isGranted,
            )
        }
    }

    private fun onReset() {
        voiceToTextParser.reset()
        _state.update {
            VoiceToTextState()
        }
    }


}