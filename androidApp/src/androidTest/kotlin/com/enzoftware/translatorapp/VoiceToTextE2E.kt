package com.enzoftware.translatorapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.enzoftware.translatorapp.android.MainActivity
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.di.AppModule
import com.enzoftware.translatorapp.android.settings.di.SettingsModule
import com.enzoftware.translatorapp.android.translate.presentation.components.TranslateTextFieldTestTags
import com.enzoftware.translatorapp.android.voice_to_text.di.VoiceToTextModule
import com.enzoftware.translatorapp.android.voice_to_text.presentation.VoiceToTextTestTags
import com.enzoftware.translatorapp.translate.data.remote.FakeTranslateClient
import com.enzoftware.translatorapp.translate.domain.translate.TranslateClient
import com.enzoftware.translatorapp.voice_to_text.data.FakeVoiceToTextParser
import com.enzoftware.translatorapp.voice_to_text.domain.VoiceToTextParser
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, VoiceToTextModule::class, SettingsModule::class)
class VoiceToTextE2E {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var voiceParser: VoiceToTextParser

    @Inject
    lateinit var client: TranslateClient

    private val parser get() = voiceParser as FakeVoiceToTextParser

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun startRecording() {
        composeRule.awaitDescription(str(R.string.start_recording)).performClick()
        composeRule.awaitText(str(R.string.listening_in, "English"))
    }

    private fun stopRecording() {
        composeRule.awaitDescription(str(R.string.stop_recording)).performClick()
        composeRule.awaitTag(VoiceToTextTestTags.SPOKEN_TEXT)
    }

    private fun closeVoiceScreen() {
        composeRule.onNodeWithContentDescription(str(R.string.cd_close_voice)).performClick()
        composeRule.awaitTag(TranslateTextFieldTestTags.EDITOR)
    }

    // ─── States ───────────────────────────────────────────────────────────────

    @Test
    fun opening_showsTheReadyCardForTheSourceLanguage() {
        composeRule.openVoiceScreen()

        composeRule.onNodeWithText(str(R.string.voice_in, "English")).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.voice_ready_message, "English")).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(str(R.string.start_recording)).assertIsDisplayed()
    }

    @Test
    fun voiceScreen_followsTheSelectedSourceLanguage() {
        composeRule.chooseFromLanguage("de")
        composeRule.awaitDescription(str(R.string.cd_choose_from_language, "German"))

        composeRule.awaitDescription(str(R.string.open_voice_translator)).performClick()

        composeRule.awaitText(str(R.string.voice_in, "German"))
    }

    @Test
    fun recording_showsTheListeningState() {
        composeRule.openVoiceScreen()
        startRecording()

        composeRule.onNodeWithText(str(R.string.listening_carefully)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(str(R.string.stop_recording)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.voice_ready_title)).assertDoesNotExist()
    }

    @Test
    fun stopping_showsTheCapturedSpeechWithApplyAndRecordAgain() {
        composeRule.openVoiceScreen()
        startRecording()
        stopRecording()

        composeRule.onNodeWithTag(VoiceToTextTestTags.SPOKEN_TEXT).assertTextEquals(parser.voiceResult)
        composeRule.onNodeWithText(str(R.string.captured_speech)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(str(R.string.apply)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(str(R.string.record_again)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.listening_in, "English")).assertDoesNotExist()
    }

    // ─── Apply ────────────────────────────────────────────────────────────────

    @Test
    fun apply_fillsTheEditor_andTheResultCanBeTranslated() {
        composeRule.openVoiceScreen()
        startRecording()
        stopRecording()

        composeRule.onNodeWithContentDescription(str(R.string.apply)).performClick()

        composeRule.awaitTag(TranslateTextFieldTestTags.EDITOR).assertTextEquals(parser.voiceResult)
        composeRule.clickTranslate()
        composeRule.awaitTag(TranslateTextFieldTestTags.TRANSLATED_TEXT)
            .assertTextEquals((client as FakeTranslateClient).translatedText)
    }

    @Test
    fun recordAgain_thenApply_submitsTheLatestResult() {
        composeRule.openVoiceScreen()
        startRecording()
        stopRecording()

        composeRule.onNodeWithContentDescription(str(R.string.record_again)).performClick()
        composeRule.awaitText(str(R.string.listening_in, "English"))
        stopRecording()

        composeRule.onNodeWithContentDescription(str(R.string.record_again)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(str(R.string.apply)).performClick()
        composeRule.awaitTag(TranslateTextFieldTestTags.EDITOR).assertTextEquals(parser.voiceResult)
    }

    // ─── Close ────────────────────────────────────────────────────────────────

    @Test
    fun closeFromReady_leavesTheEditorEmpty() {
        composeRule.openVoiceScreen()
        closeVoiceScreen()

        composeRule.onNodeWithTag(TranslateTextFieldTestTags.EDITOR).assertTextEquals("")
    }

    @Test
    fun closeWhileRecording_leavesTheEditorEmpty() {
        composeRule.openVoiceScreen()
        startRecording()
        closeVoiceScreen()

        composeRule.onNodeWithTag(TranslateTextFieldTestTags.EDITOR).assertTextEquals("")
    }

    @Test
    fun closeFromResults_discardsTheSpeech() {
        composeRule.openVoiceScreen()
        startRecording()
        stopRecording()
        closeVoiceScreen()

        composeRule.onNodeWithTag(TranslateTextFieldTestTags.EDITOR).assertTextEquals("")
    }
}
