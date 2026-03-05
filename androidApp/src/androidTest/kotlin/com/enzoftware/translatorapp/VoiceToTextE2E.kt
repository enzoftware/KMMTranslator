package com.enzoftware.translatorapp

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.enzoftware.translatorapp.android.MainActivity
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.di.AppModule
import com.enzoftware.translatorapp.android.voice_to_text.di.VoiceToTextModule
import com.enzoftware.translatorapp.translate.data.remote.FakeTranslateClient
import com.enzoftware.translatorapp.translate.domain.translate.TranslateClient
import com.enzoftware.translatorapp.voice_to_text.data.FakeVoiceToTextParser
import com.enzoftware.translatorapp.voice_to_text.domain.VoiceToTextParser
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, VoiceToTextModule::class)
class VoiceToTextE2E {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.RECORD_AUDIO,
    )

    @Inject
    lateinit var voiceParser: VoiceToTextParser

    @Inject
    lateinit var client: TranslateClient

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    // ─── Happy path ──────────────────────────────────────────────────────────────

    /**
     * Full happy-path: open the voice screen, record a result, apply it, then
     * translate and verify both the source and translated texts are displayed.
     */
    @Test
    fun recordAndTranslate() = runBlocking<Unit> {
        val parser = voiceParser as FakeVoiceToTextParser
        val fakeClient = client as FakeTranslateClient

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.record_audio))
            .performClick()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.record_audio))
            .performClick()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.stop_recording))
            .performClick()

        composeRule
            .onNodeWithText(parser.voiceResult)
            .assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.apply))
            .performClick()

        composeRule
            .onNodeWithText(parser.voiceResult)
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(context.getString(R.string.translate), ignoreCase = true)
            .performClick()

        composeRule
            .onNodeWithText(parser.voiceResult)
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(fakeClient.translatedText)
            .assertIsDisplayed()
    }

    // ─── Initial state ────────────────────────────────────────────────────────────

    /**
     * When VoiceToTextScreen first opens it should display the idle prompt and
     * show the mic FAB, indicating the user can start a recording.
     */
    @Test
    fun voiceToTextScreen_showsWaitingState_onOpen() {
        navigateToVoiceToTextScreen()

        composeRule
            .onNodeWithText(context.getString(R.string.start_talking))
            .assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.record_audio))
            .assertIsDisplayed()
    }

    // ─── Speaking state ───────────────────────────────────────────────────────────

    /**
     * Tapping the mic FAB should transition the screen to the SPEAKING state:
     * the "Listening …" label and the stop-recording FAB must be visible.
     */
    @Test
    fun voiceToTextScreen_showsSpeakingState_afterStartRecording() {
        navigateToVoiceToTextScreen()
        startRecording()

        composeRule
            .onNodeWithText(context.getString(R.string.listening))
            .assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.stop_recording))
            .assertIsDisplayed()
    }

    /**
     * While recording is active the "waiting to talk" prompt must no longer be
     * shown to the user.
     */
    @Test
    fun voiceToTextScreen_hidesIdlePrompt_whileSpeaking() {
        navigateToVoiceToTextScreen()
        startRecording()

        composeRule
            .onNodeWithText(context.getString(R.string.start_talking))
            .assertIsNotDisplayed()
    }

    // ─── Results state ────────────────────────────────────────────────────────────

    /**
     * After stopping a recording the screen should show the recognised text, the
     * apply (check) FAB, and the refresh (record-again) button.
     */
    @Test
    fun voiceToTextScreen_showsResultsState_afterStopRecording() {
        val parser = voiceParser as FakeVoiceToTextParser

        navigateToVoiceToTextScreen()
        startRecording()
        stopRecording()

        composeRule
            .onNodeWithText(parser.voiceResult)
            .assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.apply))
            .assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.record_again))
            .assertIsDisplayed()
    }

    /**
     * The "Listening …" label must not be visible once the recording has stopped
     * and results are being displayed.
     */
    @Test
    fun voiceToTextScreen_hidesListeningLabel_afterStopRecording() {
        navigateToVoiceToTextScreen()
        startRecording()
        stopRecording()

        composeRule
            .onNodeWithText(context.getString(R.string.listening))
            .assertIsNotDisplayed()
    }

    // ─── Apply result ─────────────────────────────────────────────────────────────

    /**
     * Tapping the apply FAB must navigate back to the translate screen with the
     * recognised text pre-filled in the source text field.
     */
    @Test
    fun voiceToTextScreen_applyResult_populatesFromTextField() {
        val parser = voiceParser as FakeVoiceToTextParser

        navigateToVoiceToTextScreen()
        startRecording()
        stopRecording()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.apply))
            .performClick()

        composeRule
            .onNodeWithText(parser.voiceResult)
            .assertIsDisplayed()
    }

    /**
     * After applying a voice result the translate button on the translate screen
     * must be visible, confirming that navigation back succeeded.
     */
    @Test
    fun voiceToTextScreen_applyResult_returnsToTranslateScreen() {
        navigateToVoiceToTextScreen()
        startRecording()
        stopRecording()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.apply))
            .performClick()

        composeRule
            .onNodeWithText(context.getString(R.string.translate), ignoreCase = true)
            .assertIsDisplayed()
    }

    // ─── Close / discard ──────────────────────────────────────────────────────────

    /**
     * Closing the voice screen from the idle state must navigate back to the
     * translate screen without populating the source text field.
     */
    @Test
    fun voiceToTextScreen_closeFromWaiting_returnsToTranslate_withoutPopulating() {
        navigateToVoiceToTextScreen()
        closeVoiceToTextScreen()

        composeRule
            .onNodeWithText(context.getString(R.string.translate), ignoreCase = true)
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(context.getString(R.string.enter_text))
            .assertIsDisplayed()
    }

    /**
     * Closing the voice screen while a recording is in progress must navigate
     * back to the translate screen without populating the source text field.
     */
    @Test
    fun voiceToTextScreen_closeWhileSpeaking_returnsToTranslate_withoutPopulating() {
        navigateToVoiceToTextScreen()
        startRecording()
        closeVoiceToTextScreen()

        composeRule
            .onNodeWithText(context.getString(R.string.translate), ignoreCase = true)
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(context.getString(R.string.enter_text))
            .assertIsDisplayed()
    }

    /**
     * Closing the voice screen after results have been shown (without tapping
     * Apply) must return to the translate screen and leave the source field empty.
     */
    @Test
    fun voiceToTextScreen_closeFromResults_returnsToTranslate_withoutPopulating() {
        navigateToVoiceToTextScreen()
        startRecording()
        stopRecording()
        closeVoiceToTextScreen()

        composeRule
            .onNodeWithText(context.getString(R.string.translate), ignoreCase = true)
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(context.getString(R.string.enter_text))
            .assertIsDisplayed()
    }

    // ─── Re-record ────────────────────────────────────────────────────────────────

    /**
     * Tapping the refresh (record-again) button from the results state must
     * restart the recording session and show the SPEAKING state UI.
     */
    @Test
    fun voiceToTextScreen_reRecord_transitionsToSpeakingState() {
        navigateToVoiceToTextScreen()
        startRecording()
        stopRecording()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.record_again))
            .performClick()

        composeRule
            .onNodeWithText(context.getString(R.string.listening))
            .assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.stop_recording))
            .assertIsDisplayed()
    }

    /**
     * After a re-record cycle the user should still be able to see the latest
     * recognised text and apply it to the translate screen.
     */
    @Test
    fun voiceToTextScreen_reRecordAndApply_submitsResult() {
        val parser = voiceParser as FakeVoiceToTextParser

        navigateToVoiceToTextScreen()
        startRecording()
        stopRecording()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.record_again))
            .performClick()

        stopRecording()

        composeRule
            .onNodeWithText(parser.voiceResult)
            .assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.apply))
            .performClick()

        composeRule
            .onNodeWithText(parser.voiceResult)
            .assertIsDisplayed()
    }

    /**
     * After a re-record cycle the refresh button must still be available when
     * results are displayed, allowing the user to keep iterating.
     */
    @Test
    fun voiceToTextScreen_reRecord_showsRefreshButtonAfterStop() {
        navigateToVoiceToTextScreen()
        startRecording()
        stopRecording()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.record_again))
            .performClick()

        stopRecording()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.record_again))
            .assertIsDisplayed()
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    /** Taps the mic FAB on TranslateScreen to open VoiceToTextScreen. */
    private fun navigateToVoiceToTextScreen() {
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.record_audio))
            .performClick()
    }

    /** Taps the mic FAB on VoiceToTextScreen (WAITING_TO_TALK state) to begin listening. */
    private fun startRecording() {
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.record_audio))
            .performClick()
    }

    /** Taps the stop FAB on VoiceToTextScreen (SPEAKING state) to finish listening. */
    private fun stopRecording() {
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.stop_recording))
            .performClick()
    }

    /**
     * Taps the top-left close button on VoiceToTextScreen to discard the session
     * and navigate back without submitting a result.
     *
     * Uses [substring] = true to handle the trailing newline in R.string.close.
     */
    private fun closeVoiceToTextScreen() {
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.close), substring = true)
            .performClick()
    }
}
