package com.enzoftware.translatorapp.android.voice_to_text.domain

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.SpeechRecognizer.ERROR_CLIENT
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.core.util.CommonStateFlow
import com.enzoftware.translatorapp.core.util.toCommonStateFlow
import com.enzoftware.translatorapp.voice_to_text.domain.VoiceToTextParser
import com.enzoftware.translatorapp.voice_to_text.domain.VoiceToTextParserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class AndroidVoiceToTextParser(
    private val app: Application,
) : VoiceToTextParser, RecognitionListener {

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(app)
    private val _state = MutableStateFlow(VoiceToTextParserState())

    override val state: CommonStateFlow<VoiceToTextParserState>
        get() = _state.toCommonStateFlow()

    override fun startListening(languageCode: String) {
        _state.update {
            VoiceToTextParserState()
        }

        if (!SpeechRecognizer.isRecognitionAvailable(app)) {
            _state.update {
                it.copy(error = app.getString(R.string.error_speech_not_available))
            }
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        }

        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)
        _state.update {
            it.copy(isSpeaking = true)
        }
    }

    override fun stopListening() {
        _state.update { VoiceToTextParserState() }
        recognizer.stopListening()
    }

    override fun cancel() {
        recognizer.cancel()
    }

    override fun reset() {
        _state.value = VoiceToTextParserState()
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _state.update { it.copy(error = null) }
    }

    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(rmsdB: Float) {
        _state.update {
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            it.copy(powerRatio = normalized)
        }
    }

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        _state.update {
            it.copy(isSpeaking = false)
        }
    }

    override fun onError(error: Int) {
        if (error == ERROR_CLIENT) {
            _state.update { it.copy(isSpeaking = false) }
            return
        }
        _state.update {
            it.copy(error = app.getString(R.string.unknown_error), isSpeaking = false)
        }
    }

    override fun onResults(results: Bundle?) {
        results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.getOrNull(0)
            ?.let { text ->
                _state.update {
                    it.copy(result = text)
                }
            }
    }

    override fun onPartialResults(partialResults: Bundle?) = Unit

    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}