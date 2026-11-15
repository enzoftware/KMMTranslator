package com.enzoftware.translatorapp.android.voice_to_text

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.voice_to_text.presentation.MicPermission
import com.enzoftware.translatorapp.android.voice_to_text.presentation.VoiceIssue
import com.enzoftware.translatorapp.android.voice_to_text.presentation.VoicePermissionDiagnostics
import org.junit.Test

class VoicePermissionDiagnosticsTest {

    private fun diagnostics(
        mic: MicPermission = MicPermission.GRANTED,
        speech: Boolean = true,
        error: String? = null,
    ) = VoicePermissionDiagnostics(mic, speech, error)

    @Test
    fun `everything granted means no issue`() {
        val d = diagnostics()
        assertThat(d.issue).isEqualTo(VoiceIssue.NONE)
        assertThat(d.hasMissingPermission).isFalse()
        assertThat(d.needsSettingsRedirect).isFalse()
    }

    @Test
    fun `a mic that was never granted can be requested in-app`() {
        listOf(MicPermission.NOT_ASKED, MicPermission.DENIED).forEach { mic ->
            val d = diagnostics(mic = mic)
            assertThat(d.issue, "$mic").isEqualTo(VoiceIssue.MIC_NEEDED)
            assertThat(d.hasMissingPermission).isTrue()
            assertThat(d.needsSettingsRedirect).isFalse()
            assertThat(d.headlineRes).isEqualTo(R.string.voice_mic_needed_title)
        }
    }

    @Test
    fun `a blocked mic redirects to system settings`() {
        val d = diagnostics(mic = MicPermission.BLOCKED)
        assertThat(d.issue).isEqualTo(VoiceIssue.MIC_BLOCKED)
        assertThat(d.needsSettingsRedirect).isTrue()
        assertThat(d.headlineRes).isEqualTo(R.string.voice_mic_blocked_title)
    }

    @Test
    fun `permission problems win over a missing speech service`() {
        assertThat(diagnostics(mic = MicPermission.BLOCKED, speech = false).issue).isEqualTo(VoiceIssue.MIC_BLOCKED)
        assertThat(diagnostics(mic = MicPermission.DENIED, speech = false).issue).isEqualTo(VoiceIssue.MIC_NEEDED)
    }

    @Test
    fun `missing speech recognition redirects to settings`() {
        val d = diagnostics(speech = false)
        assertThat(d.issue).isEqualTo(VoiceIssue.SPEECH_UNAVAILABLE)
        assertThat(d.needsSettingsRedirect).isTrue()
        assertThat(d.speechLabelRes).isEqualTo(R.string.speech_unavailable)
    }

    @Test
    fun `a recognizer error is reported as a recording error`() {
        val d = diagnostics(error = "Unknown error")
        assertThat(d.issue).isEqualTo(VoiceIssue.RECORDING_ERROR)
        assertThat(d.needsSettingsRedirect).isFalse()
        assertThat(d.headlineRes).isEqualTo(R.string.voice_error_title)
    }
}
