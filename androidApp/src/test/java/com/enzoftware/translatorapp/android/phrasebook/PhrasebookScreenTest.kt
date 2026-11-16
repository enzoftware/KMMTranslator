package com.enzoftware.translatorapp.android.phrasebook

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.containsExactly
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.phrasebook.presentation.PhrasebookScreen
import com.enzoftware.translatorapp.android.phrasebook.presentation.PhrasebookTestTags
import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.phrasebook.domain.PhrasebookCategory
import com.enzoftware.translatorapp.phrasebook.presentation.PhrasebookEvent
import com.enzoftware.translatorapp.phrasebook.presentation.PhrasebookState
import com.enzoftware.translatorapp.phrasebook.presentation.UiPhrase
import com.enzoftware.translatorapp.phrasebook.presentation.UiSavedWord
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(qualifiers = "w411dp-h1600dp-xxhdpi")
@RunWith(AndroidJUnit4::class)
class PhrasebookScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private fun str(id: Int, vararg args: Any) = context.getString(id, *args)

    private val events = mutableListOf<PhrasebookEvent>()
    private val used = mutableListOf<Triple<String, String?, String?>>()

    private val german = UiLanguage.byCode("de")
    private val savedWord = UiSavedWord("thank_you", UiLanguage.byCode("en"), german, "Thank you", "Danke")

    private fun setScreen(state: PhrasebookState) {
        composeRule.setContent {
            CozyTheme(darkTheme = false) {
                PhrasebookScreen(
                    state = state,
                    onEvent = { events += it },
                    onUsePhrase = { key, from, to -> used += Triple(key, from, to) },
                    onBack = {},
                )
            }
        }
    }

    private val suggestions = listOf(
        UiPhrase("thank_you", PhrasebookCategory.COURTESY, "Thank you", "Danke", isSaved = true),
        UiPhrase("water", PhrasebookCategory.FOOD, "Water", "Wasser", isSaved = false),
    )

    @Test
    fun suggested_showsPhrasesWithTheirSavedState() {
        setScreen(PhrasebookState(toLanguage = german, suggestions = suggestions))

        composeRule.onNodeWithText(str(R.string.phrasebook_subtitle, "English", "German")).assertIsDisplayed()
        composeRule.onNodeWithText("Wasser").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(str(R.string.cd_unsave_word, "Thank you")).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(str(R.string.cd_save_word, "Water")).assertIsDisplayed()
    }

    @Test
    fun bookmark_togglesTheEntry_andTappingAPhraseUsesItInTheShownPair() {
        setScreen(PhrasebookState(toLanguage = german, suggestions = suggestions))

        composeRule.onNodeWithContentDescription(str(R.string.cd_save_word, "Water")).performClick()
        composeRule.onNodeWithTag(PhrasebookTestTags.phrase("water")).performClick()

        assertThat(events).containsExactly(PhrasebookEvent.ToggleSaved("water"))
        assertThat(used).containsExactly(Triple("water", "en", "de"))
    }

    @Test
    fun categoryChips_filter() {
        setScreen(PhrasebookState(suggestions = suggestions, selectedCategory = PhrasebookCategory.FOOD))

        composeRule.onNodeWithText(str(R.string.category_food)).assertIsSelected()
        composeRule.onNodeWithText(str(R.string.category_all)).performClick()

        assertThat(events).containsExactly(PhrasebookEvent.SelectCategory(null))
    }

    @Test
    fun myWords_emptyState() {
        setScreen(PhrasebookState(suggestions = suggestions))

        composeRule.onNodeWithTag(PhrasebookTestTags.TAB_MY_WORDS).performClick()

        composeRule.onNodeWithText(str(R.string.my_words_empty_title)).assertIsDisplayed()
    }

    @Test
    fun myWords_listsSavedWords_removeAndUseThem() {
        setScreen(PhrasebookState(suggestions = suggestions, savedWords = listOf(savedWord)))

        composeRule.onNodeWithText(str(R.string.tab_my_words_count, 1)).performClick()
        composeRule.onNodeWithTag(PhrasebookTestTags.savedWord(savedWord)).performClick()
        composeRule.onNodeWithContentDescription(str(R.string.cd_remove_saved_word, "Thank you")).performClick()

        assertThat(used).containsExactly(Triple("thank_you", "en", "de"))
        assertThat(events).containsExactly(PhrasebookEvent.RemoveSavedWord("thank_you", "en", "de"))
    }
}
