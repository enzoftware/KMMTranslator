package com.enzoftware.translatorapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.enzoftware.translatorapp.android.MainActivity
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.di.AppModule
import com.enzoftware.translatorapp.android.settings.di.SettingsModule
import com.enzoftware.translatorapp.android.translate.presentation.TranslateScreenTestTags
import com.enzoftware.translatorapp.android.translate.presentation.components.TranslateTextFieldTestTags
import com.enzoftware.translatorapp.android.voice_to_text.di.VoiceToTextModule
import com.enzoftware.translatorapp.translate.data.remote.FakeTranslateClient
import com.enzoftware.translatorapp.translate.domain.translate.TranslateClient
import com.enzoftware.translatorapp.translate.domain.translate.TranslateError
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, VoiceToTextModule::class, SettingsModule::class)
class TranslateE2E {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var client: TranslateClient

    private val fakeClient get() = client as FakeTranslateClient

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    // ─── Launch ───────────────────────────────────────────────────────────────

    @Test
    fun launch_showsPhrasebookHeaderEditorAndDefaultLanguages() {
        composeRule.awaitTag(TranslateScreenTestTags.GREETING_TITLE).assertTextEquals(str(R.string.pocket_phrasebook))
        composeRule.onNodeWithText(str(R.string.enter_text)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.translate)).assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(str(R.string.cd_choose_from_language, "English")).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(str(R.string.cd_choose_to_language, "Spanish")).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.empty_shelf_title)).assertIsDisplayed()
    }

    // ─── Translating ──────────────────────────────────────────────────────────

    @Test
    fun translate_showsSourceAndTranslationWithActions() {
        composeRule.typeSource("Hello")
        composeRule.clickTranslate()

        composeRule.awaitTag(TranslateTextFieldTestTags.TRANSLATED_TEXT).assertTextEquals(fakeClient.translatedText)
        composeRule.onNodeWithTag(TranslateTextFieldTestTags.TAP_TO_EDIT).assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription(str(R.string.cd_copy_text)).assertCountEquals(2)
        composeRule.onNodeWithContentDescription(str(R.string.cd_speak_translation)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.enter_text)).assertDoesNotExist()
    }

    @Test
    fun starterIdiom_canBeTranslatedDirectly() {
        composeRule.awaitText(str(R.string.idiom_break_a_leg)).performClick()
        composeRule.clickTranslate()

        composeRule.awaitTag(TranslateTextFieldTestTags.TRANSLATED_TEXT).assertTextEquals(fakeClient.translatedText)
        composeRule.onNode(hasTestTag(TranslateTextFieldTestTags.TAP_TO_EDIT) and hasText(str(R.string.idiom_break_a_leg), substring = true))
            .assertIsDisplayed()
    }

    @Test
    fun emptyText_cannotBeTranslated() {
        composeRule.awaitText(str(R.string.translate)).assertIsNotEnabled()
        composeRule.clickTranslate()

        composeRule.onNodeWithTag(TranslateTextFieldTestTags.TRANSLATED_TEXT).assertDoesNotExist()
    }

    // ─── Close / edit ─────────────────────────────────────────────────────────

    @Test
    fun closeTranslation_returnsToEmptyEditor_andSavesAPostcard() {
        composeRule.typeSource("Hello")
        composeRule.clickTranslate()
        composeRule.awaitTag(TranslateTextFieldTestTags.TRANSLATED_TEXT)
        composeRule.closeTranslation()

        composeRule.awaitText(str(R.string.enter_text))
        composeRule.onNode(hasSetTextAction()).assertTextEquals("")
        composeRule.onNodeWithTag(TranslateScreenTestTags.HISTORY_COUNT).assertTextEquals("1")
        composeRule.onNodeWithTag(TranslateScreenTestTags.POSTCARD_SHELF).assertIsDisplayed()
    }

    @Test
    fun tapToEdit_keepsTheSourceTextEditable() {
        composeRule.typeSource("Hello")
        composeRule.clickTranslate()
        composeRule.awaitTag(TranslateTextFieldTestTags.TAP_TO_EDIT).performClick()

        composeRule.awaitTag(TranslateTextFieldTestTags.EDITOR).assertTextEquals("Hello")
        composeRule.onNodeWithTag(TranslateTextFieldTestTags.TRANSLATED_TEXT).assertDoesNotExist()
    }

    // ─── Languages ────────────────────────────────────────────────────────────

    @Test
    fun swapLanguages_afterTranslation_swapsTextsAndLanguages() {
        composeRule.typeSource("Hello")
        composeRule.clickTranslate()
        composeRule.awaitTag(TranslateTextFieldTestTags.TRANSLATED_TEXT)

        composeRule.onNodeWithContentDescription(str(R.string.swap_languages)).performClick()

        composeRule.awaitTag(TranslateTextFieldTestTags.TRANSLATED_TEXT).assertTextEquals("Hello")
        composeRule.awaitDescription(str(R.string.cd_choose_from_language, "Spanish"))
        composeRule.onNodeWithContentDescription(str(R.string.cd_choose_to_language, "English")).assertIsDisplayed()
    }

    @Test
    fun chooseFromLanguage_updatesThePill_withoutTranslating() {
        composeRule.typeSource("Hello")
        composeRule.chooseFromLanguage("de")

        composeRule.awaitDescription(str(R.string.cd_choose_from_language, "German"))
        composeRule.onNodeWithTag(TranslateTextFieldTestTags.TRANSLATED_TEXT).assertDoesNotExist()
    }

    @Test
    fun chooseToLanguage_withText_translatesImmediately() {
        composeRule.typeSource("Hello")
        composeRule.chooseToLanguage("it")

        composeRule.awaitTag(TranslateTextFieldTestTags.TRANSLATED_TEXT).assertTextEquals(fakeClient.translatedText)
        composeRule.onNodeWithContentDescription(str(R.string.cd_choose_to_language, "Italian")).assertIsDisplayed()
    }

    // ─── History ──────────────────────────────────────────────────────────────

    @Test
    fun selectingAPostcard_restoresThatTranslation() {
        composeRule.typeSource("Hello")
        composeRule.clickTranslate()
        composeRule.awaitTag(TranslateTextFieldTestTags.TRANSLATED_TEXT)
        composeRule.closeTranslation()

        composeRule.awaitText(fakeClient.translatedText).performClick()

        composeRule.awaitTag(TranslateTextFieldTestTags.TRANSLATED_TEXT).assertTextEquals(fakeClient.translatedText)
        composeRule.onNode(hasTestTag(TranslateTextFieldTestTags.TAP_TO_EDIT) and hasText("Hello", substring = true))
            .assertIsDisplayed()
    }

    @Test
    fun severalTranslations_canBeBrowsedAsAList() {
        listOf("Hello", "Thanks").forEach { text ->
            composeRule.typeSource(text)
            composeRule.clickTranslate()
            composeRule.awaitTag(TranslateTextFieldTestTags.TRANSLATED_TEXT)
            composeRule.closeTranslation()
            composeRule.awaitText(str(R.string.enter_text))
        }

        composeRule.onNodeWithTag(TranslateScreenTestTags.HISTORY_COUNT).assertTextEquals("2")
        composeRule.onNodeWithText(str(R.string.see_all)).performClick()

        composeRule.awaitText(str(R.string.shelf))
        composeRule.onNodeWithTag(TranslateScreenTestTags.POSTCARD_SHELF).assertDoesNotExist()
    }

    // ─── Errors ───────────────────────────────────────────────────────────────

    @Test
    fun translationError_showsDismissibleBanner_andKeepsTheText() {
        fakeClient.shouldReturnError = true
        fakeClient.translateError = TranslateError.SERVICE_UNAVAILABLE

        composeRule.typeSource("Hello")
        composeRule.clickTranslate()

        composeRule.awaitText(str(R.string.error_service_unavailable))
        composeRule.onNodeWithTag(TranslateTextFieldTestTags.EDITOR).assertTextEquals("Hello")
        composeRule.onNodeWithTag(TranslateTextFieldTestTags.TRANSLATED_TEXT).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(str(R.string.cd_dismiss_error)).performClick()
        composeRule.awaitGone(str(R.string.error_service_unavailable))
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @Test
    fun micButton_opensTheVoiceScreen() {
        composeRule.openVoiceScreen()

        composeRule.onNode(hasContentDescription(str(R.string.start_recording))).assertIsDisplayed()
    }
}
