package com.enzoftware.translatorapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.enzoftware.translatorapp.android.MainActivity
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.di.AppModule
import com.enzoftware.translatorapp.android.phrasebook.presentation.PhrasebookTestTags
import com.enzoftware.translatorapp.android.settings.di.SettingsModule
import com.enzoftware.translatorapp.android.translate.presentation.TranslateScreenTestTags
import com.enzoftware.translatorapp.android.translate.presentation.components.TranslateTextFieldTestTags
import com.enzoftware.translatorapp.android.voice_to_text.di.VoiceToTextModule
import com.enzoftware.translatorapp.phrasebook.FakeSavedWordsDataSource
import com.enzoftware.translatorapp.phrasebook.domain.SavedWordsDataSource
import com.enzoftware.translatorapp.translate.data.remote.FakeTranslateClient
import com.enzoftware.translatorapp.translate.domain.translate.TranslateClient
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, VoiceToTextModule::class, SettingsModule::class)
class PhrasebookE2E {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var savedWords: SavedWordsDataSource

    @Inject
    lateinit var client: TranslateClient

    private val words get() = (savedWords as FakeSavedWordsDataSource).words.value

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun openPhrasebook() {
        composeRule.awaitDescription(str(R.string.cd_open_phrasebook)).performClick()
        composeRule.awaitText(str(R.string.phrasebook_subtitle, "English", "Spanish"))
    }

    @Test
    fun phrasebookFollowsTheCurrentLanguagePair() {
        composeRule.chooseToLanguage("de")
        composeRule.awaitDescription(str(R.string.cd_choose_to_language, "German"))
        composeRule.awaitDescription(str(R.string.cd_open_phrasebook)).performClick()

        composeRule.awaitText(str(R.string.phrasebook_subtitle, "English", "German"))
        composeRule.onNodeWithText("Guten Morgen").assertIsDisplayed()
    }

    @Test
    fun tappingASuggestion_showsItsTranslationOffline_withoutCallingTheApi() {
        val fakeClient = client as FakeTranslateClient
        fakeClient.shouldReturnError = true // the API being down must not matter

        openPhrasebook()
        composeRule.onNodeWithTag(PhrasebookTestTags.phrase("good_morning")).performClick()

        composeRule.awaitTag(TranslateTextFieldTestTags.TRANSLATED_TEXT).assertTextEquals("Buenos días")
        composeRule.onNodeWithTag(TranslateScreenTestTags.HISTORY_COUNT).assertDoesNotExist()
        composeRule.onNodeWithText(str(R.string.error_unknown)).assertDoesNotExist()
    }

    @Test
    fun savingAndRemovingWords() {
        openPhrasebook()
        composeRule.onNodeWithContentDescription(str(R.string.cd_save_word, "Thank you")).performClick()
        composeRule.awaitDescription(str(R.string.cd_unsave_word, "Thank you"))
        assertThat(words.map { "${it.entryKey}:${it.toText}" }).containsExactly("thank_you:Gracias")

        composeRule.onNodeWithTag(PhrasebookTestTags.TAB_MY_WORDS).performClick()
        composeRule.awaitText(str(R.string.tab_my_words_count, 1))
        composeRule.onNodeWithContentDescription(str(R.string.cd_remove_saved_word, "Thank you")).performClick()

        composeRule.awaitText(str(R.string.my_words_empty_title))
        assertThat(words).isEmpty()
    }

    @Test
    fun aSavedWordOpensInItsOwnLanguagePair() {
        openPhrasebook()
        composeRule.onNodeWithText(str(R.string.category_food)).performClick() // "Water" is far down the full list
        composeRule.awaitDescription(str(R.string.cd_save_word, "Water")).performClick()
        composeRule.awaitDescription(str(R.string.cd_unsave_word, "Water"))
        composeRule.onNodeWithContentDescription(str(R.string.cd_back_to_translator)).performClick()

        // Switch the translator to another pair, then open the word saved for English → Spanish.
        composeRule.chooseToLanguage("ja")
        composeRule.awaitDescription(str(R.string.cd_choose_to_language, "Japanese"))
        composeRule.awaitDescription(str(R.string.cd_open_phrasebook)).performClick()
        composeRule.awaitText(str(R.string.tab_my_words_count, 1)).performClick()
        composeRule.awaitText("Agua").performClick()

        composeRule.awaitTag(TranslateTextFieldTestTags.TRANSLATED_TEXT).assertTextEquals("Agua")
        composeRule.onNodeWithContentDescription(str(R.string.cd_choose_to_language, "Spanish")).assertIsDisplayed()
    }
}
