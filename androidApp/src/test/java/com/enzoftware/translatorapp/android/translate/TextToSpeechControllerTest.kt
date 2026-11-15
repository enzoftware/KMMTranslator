package com.enzoftware.translatorapp.android.translate

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.enzoftware.translatorapp.android.translate.presentation.components.SpeechEngine
import com.enzoftware.translatorapp.android.translate.presentation.components.TextToSpeechController
import org.junit.Test
import java.util.Locale

class TextToSpeechControllerTest {

    private class FakeEngine : SpeechEngine {
        var languageSupported = true
        var speakSucceeds = true
        val spoken = mutableListOf<String>()
        val utteranceIds = mutableListOf<String>()
        var stops = 0
        var shutdowns = 0

        override fun setLanguage(locale: Locale) = languageSupported
        override fun speak(text: String, utteranceId: String): Boolean {
            spoken += text
            utteranceIds += utteranceId
            return speakSucceeds
        }
        override fun stop() { stops++ }
        override fun shutdown() { shutdowns++ }
    }

    private val engine = FakeEngine()
    private lateinit var initCallback: (Boolean) -> Unit
    private lateinit var callbacks: TextToSpeechController.UtteranceCallbacks

    private val controller = TextToSpeechController { onInit, cb ->
        initCallback = onInit
        callbacks = cb
        engine
    }

    @Test
    fun `a tap before the engine is ready is spoken once it initialises`() {
        controller.toggle("Hola", Locale.forLanguageTag("es"))
        assertThat(engine.spoken).isEmpty()
        assertThat(controller.isSpeaking).isFalse()

        initCallback(true)

        assertThat(engine.spoken).containsExactly("Hola")
        assertThat(controller.isSpeaking).isTrue()
    }

    @Test
    fun `a failed engine init never shows speaking`() {
        controller.toggle("Hola", Locale.ENGLISH)
        initCallback(false)

        assertThat(engine.spoken).isEmpty()
        assertThat(controller.isSpeaking).isFalse()
    }

    @Test
    fun `a rejected utterance does not get stuck on speaking`() {
        initCallback(true)
        engine.speakSucceeds = false

        controller.toggle("Hola", Locale.ENGLISH)

        assertThat(controller.isSpeaking).isFalse()
    }

    @Test
    fun `an unsupported language is not spoken`() {
        initCallback(true)
        engine.languageSupported = false

        controller.toggle("Merhaba", Locale.forLanguageTag("tr"))

        assertThat(engine.spoken).isEmpty()
        assertThat(controller.isSpeaking).isFalse()
    }

    @Test
    fun `finishing the active utterance resets the flag, stale ones do not`() {
        initCallback(true)
        controller.toggle("one", Locale.ENGLISH)
        val first = engine.utteranceIds.last()

        callbacks.onFinished("some-other-id")
        assertThat(controller.isSpeaking).isTrue()

        callbacks.onFinished(first)
        assertThat(controller.isSpeaking).isFalse()
    }

    @Test
    fun `toggling while speaking stops, and shutdown releases the engine`() {
        initCallback(true)
        controller.toggle("one", Locale.ENGLISH)

        controller.toggle("one", Locale.ENGLISH)
        assertThat(controller.isSpeaking).isFalse()
        assertThat(engine.stops > 0).isTrue()

        controller.shutdown()
        assertThat(engine.shutdowns > 0).isTrue()
    }

    @Test
    fun `toggling a queued request cancels it`() {
        controller.toggle("Hola", Locale.ENGLISH)
        controller.toggle("Hola", Locale.ENGLISH)

        initCallback(true)

        assertThat(engine.spoken).isEmpty()
    }
}
