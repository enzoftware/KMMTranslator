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

class ClassifyMicPermissionResultTest {

    private fun classify(granted: Boolean = false, before: Boolean = false, after: Boolean = false, deniedBefore: Boolean = false) =
        com.enzoftware.translatorapp.android.voice_to_text.presentation.classifyMicPermissionResult(granted, before, after, deniedBefore)

    @Test
    fun `granting always wins`() {
        assertThat(classify(granted = true, before = true, deniedBefore = true)).isEqualTo(MicPermission.GRANTED)
    }

    @Test
    fun `dismissing the very first dialog is not a block`() {
        assertThat(classify()).isEqualTo(MicPermission.DENIED)
    }

    @Test
    fun `tapping dont allow can still be asked again`() {
        assertThat(classify(after = true)).isEqualTo(MicPermission.DENIED)
        assertThat(classify(before = true, after = true, deniedBefore = true)).isEqualTo(MicPermission.DENIED)
    }

    @Test
    fun `a second denial makes it permanent`() {
        assertThat(classify(before = true, after = false)).isEqualTo(MicPermission.BLOCKED)
    }

    @Test
    fun `an auto-denied request after an earlier denial is blocked`() {
        // Next app session: no rationale before or after, but a denial was remembered.
        assertThat(classify(deniedBefore = true)).isEqualTo(MicPermission.BLOCKED)
    }
}
