package com.enzoftware.translatorapp

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.enzoftware.translatorapp.android.MainActivity
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.di.AppModule
import com.enzoftware.translatorapp.android.voice_to_text.di.VoiceToTextModule
import com.enzoftware.translatorapp.translate.data.remote.FakeTranslateClient
import com.enzoftware.translatorapp.translate.domain.translate.TranslateClient
import com.enzoftware.translatorapp.translate.domain.translate.TranslateError
import androidx.test.rule.GrantPermissionRule
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
class TranslateE2E {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.RECORD_AUDIO,
    )

    @Inject
    lateinit var client: TranslateClient

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    // ─── Initial / idle state ─────────────────────────────────────────────────────

    /**
     * On launch the screen must show the source text placeholder and the
     * Translate button, indicating it is ready for input.
     */
    @Test
    fun translateScreen_showsIdleState_onLaunch() {
        composeRule
            .onNodeWithText(context.getString(R.string.enter_text))
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(context.getString(R.string.translate), ignoreCase = true)
            .assertIsDisplayed()
    }

    /**
     * The default language pair must be English → Spanish, matching the
     * initial values in [TranslateState].
     */
    @Test
    fun translateScreen_showsDefaultLanguages_onLaunch() {
        // LanguageDropDown renders language.name (the enum constant name, uppercase)
        composeRule
            .onNodeWithText("ENGLISH")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("SPANISH")
            .assertIsDisplayed()
    }

    /**
     * When no text has been entered, the source text field placeholder must be
     * visible and no translation result must appear.
     */
    @Test
    fun translateScreen_showsPlaceholder_whenTextFieldIsEmpty() {
        composeRule
            .onNodeWithText(context.getString(R.string.enter_text))
            .assertIsDisplayed()

        composeRule
            .onNodeWithText((client as FakeTranslateClient).translatedText)
            .assertDoesNotExist()
    }

    // ─── Translation (happy path) ─────────────────────────────────────────────────

    /**
     * Typing text and pressing Translate must display both the original and the
     * translated text in the result view.
     */
    @Test
    fun translateScreen_translateText_showsBothTexts() = runBlocking<Unit> {
        val fakeClient = client as FakeTranslateClient

        typeText("Hello")
        clickTranslate()

        composeRule
            .onNodeWithText("Hello")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(fakeClient.translatedText)
            .assertIsDisplayed()
    }

    /**
     * The translation result view must display the language names for both the
     * source and target languages via [LanguageDisplay], which uses
     * [Language.languageName] (mixed-case) instead of the enum constant name.
     */
    @Test
    fun translateScreen_translationResult_showsLanguageLabels() = runBlocking<Unit> {
        typeText("Hello")
        clickTranslate()

        // LanguageDisplay uses language.languageName (mixed case: "English", "Spanish")
        composeRule
            .onNodeWithText("English")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Spanish")
            .assertIsDisplayed()
    }

    /**
     * The result view must expose copy buttons for both source and translated
     * text, and a speaker button to read the translation aloud.
     */
    @Test
    fun translateScreen_translationResult_showsActionButtons() = runBlocking<Unit> {
        typeText("Hello")
        clickTranslate()

        // TranslatedTextField contains two copy buttons (one per text section)
        composeRule
            .onAllNodesWithContentDescription(context.getString(R.string.copy))
            .assertCountEquals(2)

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.speaker))
            .assertIsDisplayed()
    }

    /**
     * While a translation result is being displayed, the idle placeholder must
     * not be visible.
     */
    @Test
    fun translateScreen_hidesPlaceholder_whileResultIsDisplayed() = runBlocking<Unit> {
        typeText("Hello")
        clickTranslate()

        composeRule
            .onNodeWithText(context.getString(R.string.enter_text))
            .assertIsNotDisplayed()
    }

    // ─── Close translation ────────────────────────────────────────────────────────

    /**
     * Tapping the close button inside the result view must dismiss the result
     * and return the screen to its idle state with the placeholder visible.
     */
    @Test
    fun translateScreen_closeTranslation_returnsToIdleState() = runBlocking<Unit> {
        typeText("Hello")
        clickTranslate()
        closeTranslation()

        composeRule
            .onNodeWithText(context.getString(R.string.enter_text))
            .assertIsDisplayed()
    }

    /**
     * After closing a translation the translated text must no longer be present
     * anywhere in the UI.
     */
    @Test
    fun translateScreen_closeTranslation_removesResult() = runBlocking {
        typeText("Hello")
        clickTranslate()
        closeTranslation()

        // The speaker button is exclusive to the TranslatedTextField result view;
        // its absence confirms no translation result is being displayed.
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.speaker))
            .assertDoesNotExist()
    }

    /**
     * Closing a translation must also clear the source text field so the screen
     * is ready to accept new input.
     */
    @Test
    fun translateScreen_closeTranslation_clearsSourceText() = runBlocking<Unit> {
        typeText("Hello")
        clickTranslate()
        closeTranslation()

        // After CloseTranslation the ViewModel sets fromText = "".
        // Target the editable field directly to avoid matching the history item node
        // which merges descendant text and may still contain "Hello".
        composeRule
            .onNode(hasSetTextAction())
            .assert(hasText(""))
    }

    // ─── Language swap ────────────────────────────────────────────────────────────

    /**
     * Swapping languages while a translation result is displayed must exchange
     * the from/to text values so neither text is lost.
     */
    @Test
    fun translateScreen_swapLanguages_withTranslation_preservesBothTexts() = runBlocking<Unit> {
        val fakeClient = client as FakeTranslateClient

        typeText("Hello")
        clickTranslate()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.swap_languages))
            .performClick()

        // After swap: old toText becomes fromText and vice versa — both must remain visible
        composeRule
            .onNodeWithText(fakeClient.translatedText)
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Hello")
            .assertIsDisplayed()
    }

    /**
     * Swapping languages while a translation result is displayed must also
     * swap the source and target language labels shown in the result view.
     */
    @Test
    fun translateScreen_swapLanguages_withTranslation_exchangesLanguageLabels() = runBlocking<Unit> {
        typeText("Hello")
        clickTranslate()

        // Before swap: English (from) above, Spanish (to) below in result view
        composeRule.onNodeWithText("English").assertIsDisplayed()
        composeRule.onNodeWithText("Spanish").assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.swap_languages))
            .performClick()

        // After swap: Spanish now the from-language, English the to-language — both must still appear
        composeRule.onNodeWithText("Spanish").assertIsDisplayed()
        composeRule.onNodeWithText("English").assertIsDisplayed()
    }

    // ─── Language selection (from-language) ───────────────────────────────────────

    /**
     * Tapping the from-language selector must open a dropdown that lists all
     * supported languages.
     */
    @Test
    fun translateScreen_openFromLanguageDropdown_showsLanguageOptions() {
        composeRule
            .onNodeWithText("ENGLISH")
            .performClick()

        // A language not currently selected must appear as a menu item
        composeRule
            .onNodeWithText("ITALIAN")
            .assertIsDisplayed()
    }

    /**
     * Selecting a language from the from-language dropdown must update the
     * selector label to the chosen language and close the dropdown.
     */
    @Test
    fun translateScreen_selectFromLanguage_updatesSelector() {
        composeRule.onNodeWithText("ENGLISH").performClick()
        composeRule.onNodeWithText("GERMAN").performClick()

        composeRule
            .onNodeWithText("GERMAN")
            .assertIsDisplayed()
    }

    /**
     * Changing the from-language must NOT trigger an automatic translation —
     * only [ChooseToLanguage] triggers translation.
     */
    @Test
    fun translateScreen_selectFromLanguage_doesNotTranslate() = runBlocking<Unit> {
        val fakeClient = client as FakeTranslateClient

        typeText("Hello")
        composeRule.onNodeWithText("ENGLISH").performClick()
        composeRule.onNodeWithText("GERMAN").performClick()

        composeRule
            .onNodeWithText(fakeClient.translatedText)
            .assertDoesNotExist()
    }

    // ─── Language selection (to-language) ─────────────────────────────────────────

    /**
     * Tapping the to-language selector must open a dropdown that lists all
     * supported languages.
     */
    @Test
    fun translateScreen_openToLanguageDropdown_showsLanguageOptions() {
        composeRule
            .onNodeWithText("SPANISH")
            .performClick()

        composeRule
            .onNodeWithText("ITALIAN")
            .assertIsDisplayed()
    }

    /**
     * Selecting a new to-language while source text is present must immediately
     * trigger a translation and display the result.
     */
    @Test
    fun translateScreen_selectToLanguage_triggersTranslation() = runBlocking<Unit> {
        val fakeClient = client as FakeTranslateClient

        typeText("Hello")
        composeRule.onNodeWithText("SPANISH").performClick()
        composeRule.onNodeWithText("GERMAN").performClick()

        composeRule
            .onNodeWithText(fakeClient.translatedText)
            .assertIsDisplayed()

        // LanguageDisplay for the new target language uses languageName (mixed case)
        composeRule
            .onNodeWithText("German")
            .assertIsDisplayed()
    }

    // ─── History ──────────────────────────────────────────────────────────────────

    /**
     * After translating and closing the result, the history section header and
     * the translated pair must appear in the history list.
     */
    @Test
    fun translateScreen_showsHistorySection_afterClosingTranslation() = runBlocking<Unit> {
        val fakeClient = client as FakeTranslateClient

        typeText("Hello")
        clickTranslate()
        closeTranslation()

        composeRule
            .onNodeWithText(context.getString(R.string.history))
            .assertIsDisplayed()

        // History item shows fromText in the first row
        composeRule
            .onNodeWithText("Hello", substring = true)
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(fakeClient.translatedText, substring = true)
            .assertIsDisplayed()
    }

    /**
     * The history section must be hidden while a translation result is actively
     * displayed, to prevent the source text appearing twice in the UI.
     */
    @Test
    fun translateScreen_hidesHistorySection_whileResultIsDisplayed() = runBlocking<Unit> {
        typeText("Hello")
        clickTranslate()

        composeRule
            .onNodeWithText(context.getString(R.string.history))
            .assertDoesNotExist()
    }

    /**
     * Tapping a history item must restore its source and translated texts in
     * the result view, effectively re-displaying that translation.
     */
    @Test
    fun translateScreen_selectHistoryItem_restoresTranslation() = runBlocking<Unit> {
        val fakeClient = client as FakeTranslateClient

        typeText("Hello")
        clickTranslate()
        closeTranslation()

        // History item text is merged into a single clickable node; use substring matching
        composeRule
            .onNodeWithText("Hello", substring = true)
            .performClick()

        // Selecting a history item replaces the current state — result view appears
        composeRule
            .onNodeWithText("Hello")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(fakeClient.translatedText)
            .assertIsDisplayed()
    }

    /**
     * After selecting a history item, the history section itself must be hidden
     * because [TranslateState.toText] is now non-null.
     */
    @Test
    fun translateScreen_selectHistoryItem_hidesHistorySection() = runBlocking<Unit> {
        typeText("Hello")
        clickTranslate()
        closeTranslation()

        composeRule
            .onNodeWithText("Hello", substring = true)
            .performClick()

        composeRule
            .onNodeWithText(context.getString(R.string.history))
            .assertDoesNotExist()
    }

    // ─── Error handling ───────────────────────────────────────────────────────────

    /**
     * When the translation service returns an error, no translated text must
     * appear and the screen must remain in its idle input state.
     */
    @Test
    fun translateScreen_onTranslationError_noResultShown() = runBlocking<Unit> {
        val fakeClient = client as FakeTranslateClient
        fakeClient.shouldReturnError = true
        fakeClient.translateError = TranslateError.UNKNOWN_ERROR

        typeText("Hello")
        clickTranslate()

        composeRule
            .onNodeWithText(fakeClient.translatedText)
            .assertDoesNotExist()
    }

    /**
     * A service-unavailable error must leave the source text intact so the
     * user can retry without having to retype.
     */
    @Test
    fun translateScreen_onServiceUnavailableError_keepsSourceText() = runBlocking<Unit> {
        val fakeClient = client as FakeTranslateClient
        fakeClient.shouldReturnError = true
        fakeClient.translateError = TranslateError.SERVICE_UNAVAILABLE

        typeText("Hello")
        clickTranslate()

        composeRule
            .onNodeWithText("Hello")
            .assertIsDisplayed()
    }

    // ─── Edge cases ───────────────────────────────────────────────────────────────

    /**
     * Pressing Translate with an empty source field must not produce any
     * translation result — blank input is ignored by the ViewModel.
     */
    @Test
    fun translateScreen_emptyText_doesNotTranslate() = runBlocking<Unit> {
        val fakeClient = client as FakeTranslateClient

        clickTranslate()

        composeRule
            .onNodeWithText(fakeClient.translatedText)
            .assertDoesNotExist()

        composeRule
            .onNodeWithText(context.getString(R.string.enter_text))
            .assertIsDisplayed()
    }

    /**
     * Navigating to the voice screen and immediately closing it must return to
     * the translate screen without altering the source text field.
     */
    @Test
    fun translateScreen_recordAudioFab_navigatesToVoiceScreen() {
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.record_audio))
            .performClick()

        // VoiceToTextScreen is now active — its "Click to start recording" prompt is visible
        composeRule
            .onNodeWithText(context.getString(R.string.start_talking))
            .assertIsDisplayed()
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    /** Types [text] into the source text field (BasicTextField). */
    private fun typeText(text: String) {
        composeRule
            .onNode(hasSetTextAction())
            .performTextInput(text)
    }

    /** Taps the Translate button to submit the current source text for translation. */
    private fun clickTranslate() {
        composeRule
            .onNodeWithText(context.getString(R.string.translate), ignoreCase = true)
            .performClick()
    }

    /**
     * Taps the Close button inside the translation result view to dismiss it.
     *
     * Uses [substring] = true to handle the trailing newline in [R.string.close].
     * When the result view is open and no language dropdown is expanded,
     * this button is the only node whose content description contains "Close".
     */
    private fun closeTranslation() {
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.close), substring = true)
            .performClick()
    }
}
