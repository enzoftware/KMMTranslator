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
 * Minimal surface of [TextToSpeech] used by [TextToSpeechController], so the state handling can be
 * unit tested without a real engine.
 */
interface SpeechEngine {
    /** Returns false when the language is missing or unsupported. */
    fun setLanguage(locale: Locale): Boolean

    /** Returns true when the utterance was queued. */
    fun speak(text: String, utteranceId: String): Boolean
    fun stop()
    fun shutdown()
}

/**
 * Wraps text-to-speech with an observable [isSpeaking] flag, mirroring the iOS `TextToSpeech`
 * helper: calling [toggle] while speaking stops playback.
 *
 * The engine binds asynchronously, so a tap that arrives before it is ready is queued and spoken
 * once initialisation succeeds. [isSpeaking] only turns on when the engine accepted the utterance,
 * so a missing engine or unsupported language can never leave the button stuck on "Speaking".
 */
class TextToSpeechController internal constructor(
    engineFactory: (onInit: (Boolean) -> Unit, callbacks: UtteranceCallbacks) -> SpeechEngine,
) {
    var isSpeaking by mutableStateOf(false)
        private set

    private var isReady = false
    private var isShutdown = false
    private var pending: Pair<String, Locale>? = null
    private var activeUtteranceId: String? = null

    private val engine: SpeechEngine = engineFactory(::onInit, object : UtteranceCallbacks {
        override fun onFinished(utteranceId: String?) {
            if (utteranceId != null && utteranceId == activeUtteranceId) {
                activeUtteranceId = null
                isSpeaking = false
            }
        }
    })

    fun toggle(text: String, locale: Locale) {
        if (isSpeaking || pending != null) {
            stop()
            return
        }
        if (!isReady) {
            pending = text to locale
            return
        }
        speakNow(text, locale)
    }

    fun stop() {
        pending = null
        activeUtteranceId = null
        if (isReady) engine.stop()
        isSpeaking = false
    }

    fun shutdown() {
        stop()
        isShutdown = true
        engine.shutdown()
    }

    private fun onInit(success: Boolean) {
        if (isShutdown) return
        isReady = success
        val queued = pending
        pending = null
        if (success && queued != null) speakNow(queued.first, queued.second)
    }

    private fun speakNow(text: String, locale: Locale) {
        if (!engine.setLanguage(locale)) return
        val id = UUID.randomUUID().toString()
        activeUtteranceId = id
        if (engine.speak(text, id)) {
            isSpeaking = true
        } else {
            activeUtteranceId = null
        }
    }

    interface UtteranceCallbacks {
        fun onFinished(utteranceId: String?)
    }

    companion object {
        /** Creates a controller backed by the platform [TextToSpeech] engine. */
        fun create(context: Context): TextToSpeechController = TextToSpeechController { onInit, callbacks ->
            AndroidSpeechEngine(context.applicationContext, onInit, callbacks)
        }
    }
}

private class AndroidSpeechEngine(
    context: Context,
    onInit: (Boolean) -> Unit,
    callbacks: TextToSpeechController.UtteranceCallbacks,
) : SpeechEngine {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val tts: TextToSpeech = TextToSpeech(context) { status ->
        mainHandler.post { onInit(status == TextToSpeech.SUCCESS) }
    }

    init {
        // Progress callbacks arrive on a binder thread; hop to main before touching Compose state.
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) = finish(utteranceId)

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = finish(utteranceId)
            override fun onError(utteranceId: String?, errorCode: Int) = finish(utteranceId)
            override fun onStop(utteranceId: String?, interrupted: Boolean) = finish(utteranceId)

            private fun finish(id: String?) {
                mainHandler.post { callbacks.onFinished(id) }
            }
        })
    }

    override fun setLanguage(locale: Locale): Boolean =
        tts.setLanguage(locale) >= TextToSpeech.LANG_AVAILABLE

    override fun speak(text: String, utteranceId: String): Boolean =
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId) == TextToSpeech.SUCCESS

    override fun stop() {
        tts.stop()
    }

    override fun shutdown() {
        mainHandler.removeCallbacksAndMessages(null)
        tts.shutdown()
    }
}

@Composable
fun rememberTextToSpeechController(): TextToSpeechController {
    val context = LocalContext.current
    val controller = remember { TextToSpeechController.create(context) }
    DisposableEffect(controller) {
        onDispose { controller.shutdown() }
    }
    return controller
}
