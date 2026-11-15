package com.enzoftware.translatorapp.android.translate.presentation.components

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import java.util.UUID

/**
 * Wraps [TextToSpeech] with an observable [isSpeaking] flag, mirroring the iOS `TextToSpeech`
 * helper: calling [toggle] while speaking stops playback.
 */
class TextToSpeechController(context: Context) {
    var isSpeaking by mutableStateOf(false)
        private set

    private val mainHandler = Handler(Looper.getMainLooper())
    private var activeUtteranceId: String? = null
    private val tts = TextToSpeech(context.applicationContext, null)

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) = finish(utteranceId)

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = finish(utteranceId)
            override fun onError(utteranceId: String?, errorCode: Int) = finish(utteranceId)
            override fun onStop(utteranceId: String?, interrupted: Boolean) = finish(utteranceId)
        })
    }

    fun toggle(text: String, locale: Locale) {
        if (isSpeaking) {
            stop()
            return
        }
        val id = UUID.randomUUID().toString()
        activeUtteranceId = id
        tts.language = locale
        isSpeaking = true
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    fun stop() {
        activeUtteranceId = null
        tts.stop()
        isSpeaking = false
    }

    fun shutdown() {
        stop()
        tts.shutdown()
    }

    private fun finish(utteranceId: String?) {
        mainHandler.post {
            if (utteranceId == activeUtteranceId) {
                activeUtteranceId = null
                isSpeaking = false
            }
        }
    }
}

@Composable
fun rememberTextToSpeechController(): TextToSpeechController {
    val context = LocalContext.current
    val controller = remember { TextToSpeechController(context) }
    DisposableEffect(controller) {
        onDispose { controller.shutdown() }
    }
    return controller
}
