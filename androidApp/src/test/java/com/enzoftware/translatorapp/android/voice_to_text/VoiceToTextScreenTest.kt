package com.enzoftware.translatorapp.android.voice_to_text

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.voice_to_text.presentation.MicPermission
import com.enzoftware.translatorapp.android.voice_to_text.presentation.VoicePermissionDiagnostics
import com.enzoftware.translatorapp.android.voice_to_text.presentation.VoiceToTextScreen
import com.enzoftware.translatorapp.android.voice_to_text.presentation.VoiceToTextTestTags
import com.enzoftware.translatorapp.voice_to_text.presentation.DisplayState
import com.enzoftware.translatorapp.voice_to_text.presentation.VoiceToTextEvent
import com.enzoftware.translatorapp.voice_to_text.presentation.VoiceToTextState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// Tall phone-sized display so below-the-fold content is composed and clickable.
@Config(qualifiers = "w411dp-h1600dp-xxhdpi")
@RunWith(AndroidJUnit4::class)
class VoiceToTextScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private fun str(id: Int, vararg args: Any) = context.getString(id, *args)

    private val events = mutableListOf<VoiceToTextEvent>()
    private val results = mutableListOf<String>()
    private var permissionRequests = 0
    private var settingsOpened = 0

    private fun setScreen(
        state: VoiceToTextState,
        mic: MicPermission = MicPermission.GRANTED,
        speechAvailable: Boolean = true,
    ) {
        composeRule.setContent {
            CozyTheme(darkTheme = false) {
                VoiceToTextScreen(
                    state = state,
                    languageCode = "en",
                    diagnostics = VoicePermissionDiagnostics(mic, speechAvailable, state.recordErrorText),
                    onResult = { results += it },
                    onEvent = { events += it },
                    onRequestPermission = { permissionRequests++ },
                    onOpenSettings = { settingsOpened++ },
                )
            }
        }
    }

    @Test
    fun waiting_showsReadyCard_andRecordStartsListening() {
        setScreen(VoiceToTextState(canRecord = true, displayState = DisplayState.WAITING_TO_TALK))

        composeRule.onNodeWithText(str(R.string.voice_ready_title)).assertIsDisplayed()
        composeRule.onNodeWithTag(VoiceToTextTestTags.STATUS).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.voice_in, "English")).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(str(R.string.start_recording)).performClick()

        assertThat(events).containsExactly(VoiceToTextEvent.ToggleRecording("en"))
    }

    @Test
    fun speaking_showsListeningStatus_andStopTogglesRecording() {
        setScreen(
            VoiceToTextState(canRecord = true, powerRatios = listOf(0.2f, 0.8f), displayState = DisplayState.SPEAKING),
        )

        composeRule.onNodeWithText(str(R.string.listening_in, "English")).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.listening_carefully)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(str(R.string.stop_recording)).performClick()

        assertThat(events).containsExactly(VoiceToTextEvent.ToggleRecording("en"))
    }

    @Test
    fun results_applyReturnsTheSpokenText() {
        setScreen(
            VoiceToTextState(canRecord = true, spokenText = "good morning", displayState = DisplayState.DISPLAYING_RESULTS),
        )

        composeRule.onNodeWithText(str(R.string.captured_speech)).assertIsDisplayed()
        composeRule.onNodeWithTag(VoiceToTextTestTags.SPOKEN_TEXT).assertTextEquals("good morning")

        composeRule.onNodeWithContentDescription(str(R.string.apply)).performClick()

        assertThat(results).containsExactly("good morning")
        assertThat(events).isEmpty()
    }

    @Test
    fun results_recordAgainRestartsListening() {
        setScreen(
            VoiceToTextState(canRecord = true, spokenText = "good morning", displayState = DisplayState.DISPLAYING_RESULTS),
        )

        composeRule.onNodeWithContentDescription(str(R.string.record_again)).performClick()

        assertThat(events).containsExactly(VoiceToTextEvent.ToggleRecording("en"))
        assertThat(results).isEmpty()
    }

    @Test
    fun recognizerError_showsMessageAndTryAgain() {
        setScreen(
            VoiceToTextState(canRecord = true, recordErrorText = "Unknown error", displayState = DisplayState.ERROR),
        )

        composeRule.onNodeWithText(str(R.string.voice_error_title)).assertIsDisplayed()
        composeRule.onNodeWithText("Unknown error").assertIsDisplayed()

        composeRule.onNodeWithText(str(R.string.try_again)).performClick()

        assertThat(events).containsExactly(VoiceToTextEvent.ToggleRecording("en"))
    }

    @Test
    fun missingMicPermission_asksForItInsteadOfRecording() {
        // Without permission the shared VM reports ERROR; the screen explains the permission instead.
        setScreen(VoiceToTextState(canRecord = false, displayState = DisplayState.ERROR), mic = MicPermission.DENIED)

        composeRule.onNodeWithText(str(R.string.voice_mic_needed_title)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.permission_mic, str(R.string.permission_denied))).assertIsDisplayed()

        composeRule.onNodeWithText(str(R.string.allow_microphone)).performClick()
        composeRule.onNodeWithContentDescription(str(R.string.start_recording)).performClick()

        assertThat(permissionRequests).isEqualTo(2)
        assertThat(events).isEmpty()
    }

    @Test
    fun blockedMicPermission_pointsToSystemSettings() {
        setScreen(VoiceToTextState(canRecord = false, displayState = DisplayState.ERROR), mic = MicPermission.BLOCKED)

        composeRule.onNodeWithText(str(R.string.voice_mic_blocked_title)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.open_settings)).performClick()

        assertThat(settingsOpened).isEqualTo(1)
        assertThat(permissionRequests).isEqualTo(0)
    }

    @Test
    fun speechServiceUnavailable_explainsAndOffersSettings() {
        setScreen(
            VoiceToTextState(canRecord = true, displayState = DisplayState.WAITING_TO_TALK),
            speechAvailable = false,
        )

        composeRule.onNodeWithText(str(R.string.voice_speech_unavailable_title)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.permission_speech, str(R.string.speech_unavailable))).assertIsDisplayed()

        // Tapping record explains the problem in a dialog instead of starting a doomed session.
        composeRule.onNodeWithContentDescription(str(R.string.start_recording)).performClick()
        composeRule.onNodeWithText(str(R.string.voice_permission_dialog_title)).assertIsDisplayed()
        assertThat(events).isEmpty()
    }

    @Test
    fun closeButton_sendsClose() {
        setScreen(VoiceToTextState(canRecord = true, displayState = DisplayState.WAITING_TO_TALK))

        composeRule.onNodeWithContentDescription(str(R.string.cd_close_voice)).performClick()

        assertThat(events).containsExactly(VoiceToTextEvent.Close)
    }
}
