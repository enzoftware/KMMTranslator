package com.enzoftware.translatorapp.android.translate

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.settings.domain.UserSettings
import com.enzoftware.translatorapp.android.translate.presentation.TranslateScreen
import com.enzoftware.translatorapp.android.translate.presentation.TranslateScreenTestTags
import com.enzoftware.translatorapp.android.translate.presentation.components.TranslateTextFieldTestTags
import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.translate.domain.translate.TranslateError
import com.enzoftware.translatorapp.translate.presentation.TranslateEvent
import com.enzoftware.translatorapp.translate.presentation.TranslateState
import com.enzoftware.translatorapp.translate.presentation.UiHistoryItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// Tall phone-sized display so below-the-fold content is composed and clickable.
@Config(qualifiers = "w411dp-h1600dp-xxhdpi")
@RunWith(AndroidJUnit4::class)
class TranslateScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private fun str(id: Int, vararg args: Any) = context.getString(id, *args)

    private val events = mutableListOf<TranslateEvent>()
    private var settingsOpened = false
    private var phrasebookOpened = false

    private fun historyItem(id: Long, from: String, to: String) = UiHistoryItem(
        id = id,
        fromText = from,
        toText = to,
        fromLanguage = UiLanguage.byCode("en"),
        toLanguage = UiLanguage.byCode("es"),
    )

    private fun setScreen(
        state: TranslateState = TranslateState(),
        settings: UserSettings = UserSettings(),
        hourOfDay: Int = 9,
    ) {
        composeRule.setContent {
            CozyTheme(darkTheme = false) {
                TranslateScreen(
                    state = state,
                    settings = settings,
                    onEvent = { events += it },
                    onOpenSettings = { settingsOpened = true },
                    onOpenPhrasebook = { phrasebookOpened = true },
                    hourOfDay = hourOfDay,
                )
            }
        }
    }

    // ─── Header ───────────────────────────────────────────────────────────────

    @Test
    fun header_showsGreetingAndPhrasebookTitle_whenNoNameIsSet() {
        setScreen(hourOfDay = 9)

        composeRule.onNodeWithText(str(R.string.greeting_morning).uppercase()).assertIsDisplayed()
        composeRule.onNodeWithTag(TranslateScreenTestTags.GREETING_TITLE)
            .assertTextEquals(str(R.string.pocket_phrasebook))
    }

    @Test
    fun header_greetsTheUserByName() {
        setScreen(settings = UserSettings(userName = "  Ada Lovelace "), hourOfDay = 23)

        composeRule.onNodeWithText(str(R.string.greeting_late_night).uppercase()).assertIsDisplayed()
        composeRule.onNodeWithTag(TranslateScreenTestTags.GREETING_TITLE)
            .assertTextEquals(str(R.string.hi_name, "Ada Lovelace"))
        composeRule.onNodeWithText("AL").assertIsDisplayed()
    }

    @Test
    fun settingsButton_opensSettings() {
        setScreen()

        composeRule.onNodeWithContentDescription(str(R.string.cd_open_settings)).performClick()

        assertThat(settingsOpened).isTrue()
    }

    @Test
    fun phrasebookButton_opensThePhrasebook() {
        setScreen()

        composeRule.onNodeWithContentDescription(str(R.string.cd_open_phrasebook)).performClick()

        assertThat(phrasebookOpened).isTrue()
    }

    // ─── Idle editor ──────────────────────────────────────────────────────────

    @Test
    fun idle_showsPlaceholderStarterIdiomsAndDisabledTranslate() {
        setScreen()

        composeRule.onNodeWithText(str(R.string.enter_text)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.idiom_break_a_leg)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.chars_count, 0)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.translate)).assertIsNotEnabled()
    }

    @Test
    fun typing_dispatchesChangeTranslationText() {
        setScreen()

        composeRule.onNodeWithTag(TranslateTextFieldTestTags.EDITOR).performTextInput("Hello")

        assertThat(events).contains(TranslateEvent.ChangeTranslationText("Hello"))
    }

    @Test
    fun starterIdiom_fillsTheEditor() {
        setScreen()

        composeRule.onNodeWithText(str(R.string.idiom_piece_of_cake)).performClick()

        assertThat(events).containsExactly(TranslateEvent.ChangeTranslationText(str(R.string.idiom_piece_of_cake)))
    }

    @Test
    fun starterIdioms_areHiddenForNonEnglishSource() {
        setScreen(TranslateState(fromLanguage = UiLanguage.byCode("de")))

        composeRule.onNodeWithText(str(R.string.idiom_break_a_leg)).assertDoesNotExist()
    }

    @Test
    fun translateButton_dispatchesTranslate_andClearClearsText() {
        setScreen(TranslateState(fromText = "Hello"))

        composeRule.onNodeWithText(str(R.string.chars_count, 5)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.translate)).performClick()
        composeRule.onNodeWithText(str(R.string.clear)).performClick()

        assertThat(events).containsExactly(TranslateEvent.Translate, TranslateEvent.CloseTranslation)
    }

    @Test
    fun translating_showsProgressAndHidesClear() {
        setScreen(TranslateState(fromText = "Hello", isTranslating = true))

        composeRule.onNodeWithText(str(R.string.translating)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.clear)).assertDoesNotExist()
    }

    // ─── Translated result ────────────────────────────────────────────────────

    @Test
    fun result_showsBothTextsWithCopyAndListenActions() {
        setScreen(TranslateState(fromText = "Hello", toText = "Hola"))

        composeRule.onNodeWithText("Hello").assertIsDisplayed()
        composeRule.onNodeWithTag(TranslateTextFieldTestTags.TRANSLATED_TEXT).assertTextEquals("Hola")
        composeRule.onAllNodesWithContentDescription(str(R.string.cd_copy_text)).assertCountEquals(2)
        composeRule.onNodeWithContentDescription(str(R.string.cd_speak_translation)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.enter_text)).assertDoesNotExist()
    }

    @Test
    fun result_closeAndTapToEdit_dispatchEvents() {
        setScreen(TranslateState(fromText = "Hello", toText = "Hola"))

        composeRule.onNodeWithTag(TranslateTextFieldTestTags.TAP_TO_EDIT).performClick()
        composeRule.onNodeWithContentDescription(str(R.string.cd_close_translation)).performClick()

        assertThat(events).containsExactly(TranslateEvent.EditTranslation, TranslateEvent.CloseTranslation)
    }

    // ─── Saved postcards ──────────────────────────────────────────────────────

    @Test
    fun emptyHistory_showsWaitingShelf() {
        setScreen()

        composeRule.onNodeWithText(str(R.string.saved_postcards)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.empty_shelf_title)).assertIsDisplayed()
        composeRule.onNodeWithTag(TranslateScreenTestTags.HISTORY_COUNT).assertDoesNotExist()
        composeRule.onNodeWithText(str(R.string.see_all)).assertDoesNotExist()
    }

    @Test
    fun history_showsCountAndPostcards_andSelectingRestoresIt() {
        val item = historyItem(1, "Break a leg tonight!", "¡Mucha suerte esta noche!")
        setScreen(TranslateState(history = listOf(item)))

        composeRule.onNodeWithTag(TranslateScreenTestTags.HISTORY_COUNT).assertTextEquals("1")
        composeRule.onNodeWithTag(TranslateScreenTestTags.POSTCARD_SHELF).assertIsDisplayed()
        // A single postcard has no list view to switch to.
        composeRule.onNodeWithText(str(R.string.see_all)).assertDoesNotExist()

        composeRule.onNodeWithText("¡Mucha suerte esta noche!").performClick()

        assertThat(events).containsExactly(TranslateEvent.SelectHistoryItem(item))
    }

    @Test
    fun history_staysVisibleWhileAResultIsShown() {
        val item = historyItem(1, "Hello", "Hola")
        setScreen(TranslateState(fromText = "Hello", toText = "Hola", history = listOf(item)))

        composeRule.onNodeWithText(str(R.string.saved_postcards)).assertIsDisplayed()
        composeRule.onNodeWithTag(TranslateScreenTestTags.HISTORY_COUNT).assertTextEquals("1")
    }

    @Test
    fun seeAll_togglesBetweenShelfAndList() {
        setScreen(
            TranslateState(
                history = listOf(historyItem(1, "Hello", "Hola"), historyItem(2, "Thanks", "Gracias")),
            ),
        )

        composeRule.onNodeWithText(str(R.string.see_all)).performClick()
        composeRule.onNodeWithTag(TranslateScreenTestTags.POSTCARD_SHELF).assertDoesNotExist()
        composeRule.onNodeWithText("Gracias").assertIsDisplayed()

        composeRule.onNodeWithText(str(R.string.shelf)).performClick()
        composeRule.onNodeWithTag(TranslateScreenTestTags.POSTCARD_SHELF).assertIsDisplayed()
    }

    // ─── Dock ─────────────────────────────────────────────────────────────────

    @Test
    fun dock_swapAndMic_dispatchEvents() {
        setScreen()

        composeRule.onNodeWithContentDescription(str(R.string.swap_languages)).performClick()
        composeRule.onNodeWithContentDescription(str(R.string.open_voice_translator)).performClick()

        assertThat(events).containsExactly(TranslateEvent.SwapLanguages, TranslateEvent.RecordAudio)
    }

    @Test
    fun dock_languagePills_openTheirMenus() {
        setScreen()

        composeRule.onNodeWithContentDescription(str(R.string.cd_choose_from_language, "English")).performClick()
        composeRule.onNodeWithContentDescription(str(R.string.cd_choose_to_language, "Spanish")).performClick()

        assertThat(events).containsExactly(
            TranslateEvent.OpenFromLanguageDropDown,
            TranslateEvent.OpenToLanguageDropDown,
        )
    }

    @Test
    fun openToLanguageMenu_selectingALanguageDispatchesChooseToLanguage() {
        setScreen(TranslateState(isChoosingToLanguage = true))

        composeRule.onNodeWithText("Italian").performClick()

        assertThat(events).containsExactly(TranslateEvent.ChooseToLanguage(UiLanguage.byCode("it")))
    }

    // ─── Errors ───────────────────────────────────────────────────────────────

    @Test
    fun error_showsBannerThatCanBeDismissed() {
        var state by mutableStateOf(TranslateState(error = TranslateError.SERVICE_UNAVAILABLE))
        composeRule.setContent {
            CozyTheme(darkTheme = false) {
                TranslateScreen(
                    state = state,
                    settings = UserSettings(),
                    onEvent = { events += it; if (it == TranslateEvent.OnError) state = state.copy(error = null) },
                    onOpenSettings = {},
                    hourOfDay = 9,
                )
            }
        }

        composeRule.onNodeWithText(str(R.string.error_service_unavailable)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(str(R.string.cd_dismiss_error)).performClick()

        assertThat(events).containsExactly(TranslateEvent.OnError)
        composeRule.onNodeWithText(str(R.string.error_service_unavailable)).assertDoesNotExist()
        assertThat(state.error).isEqualTo(null)
    }
}
