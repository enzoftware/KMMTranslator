package com.enzoftware.translatorapp.voice_to_text.presentation

data class VoiceToTextState(
    val powerRatios: List<Float> = emptyList(),
    val spokenText: String = "",
    val canRecord: Boolean = false,
    val recordErrorText: String? = null,
    val displayState: DisplayState? = null,
)

enum class DisplayState {
    WAITING_TO_TALK,
    SPEAKING,
    ERROR,
    DISPLAYING_RESULTS
}