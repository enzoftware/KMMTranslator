package com.enzoftware.translatorapp.voice_to_text.domain

import com.enzoftware.translatorapp.core.util.CommonStateFlow

interface VoiceToTextParser {
    val state: CommonStateFlow<VoiceToTextParserState>

    fun startListening(languageCode: String)
    fun stopListening()
    fun cancel()
    fun reset()
}