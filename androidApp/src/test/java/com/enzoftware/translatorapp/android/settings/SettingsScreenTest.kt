package com.enzoftware.translatorapp.android.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.settings.domain.AppTheme
import com.enzoftware.translatorapp.android.settings.domain.AppVersionInfo
import com.enzoftware.translatorapp.android.settings.domain.UserSettings
import com.enzoftware.translatorapp.android.settings.presentation.SettingsScreen
import com.enzoftware.translatorapp.android.settings.presentation.SettingsTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// Tall phone-sized display so below-the-fold content is composed and clickable.
@Config(qualifiers = "w411dp-h1600dp-xxhdpi")
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private fun str(id: Int, vararg args: Any) = context.getString(id, *args)

    private var settings by mutableStateOf(UserSettings())
    private val themeChanges = mutableListOf<AppTheme>()
    private var backPressed = false

    private fun setScreen(initial: UserSettings = UserSettings()) {
        settings = initial
        composeRule.setContent {
            CozyTheme(darkTheme = settings.theme.isDark) {
                SettingsScreen(
                    settings = settings,
                    versionInfo = AppVersionInfo("Parley", "1.0", "7"),
                    onUserNameChange = { settings = settings.copy(userName = it) },
                    onThemeChange = {
                        themeChanges += it
                        settings = settings.copy(theme = it)
                    },
                    onBack = { backPressed = true },
                )
            }
        }
    }

    @Test
    fun withoutName_showsWordTravelerAndDefaultInitial() {
        setScreen()

        composeRule.onNodeWithText(str(R.string.word_traveler)).assertIsDisplayed()
        composeRule.onNodeWithText("P").assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.enter_your_name)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(str(R.string.cd_clear_name)).assertDoesNotExist()
    }

    @Test
    fun typingAName_updatesGreetingAndInitials() {
        setScreen()

        composeRule.onNodeWithTag(SettingsTestTags.NAME_FIELD).performTextInput("Ada Lovelace")

        assertThat(settings.userName).isEqualTo("Ada Lovelace")
        composeRule.onNodeWithText(str(R.string.hi_name_exclaim, "Ada Lovelace")).assertIsDisplayed()
        composeRule.onNodeWithText("AL").assertIsDisplayed()
    }

    @Test
    fun clearButton_emptiesTheName() {
        setScreen(UserSettings(userName = "Ada"))

        composeRule.onNodeWithContentDescription(str(R.string.cd_clear_name)).performClick()

        assertThat(settings.userName).isEqualTo("")
        composeRule.onNodeWithText(str(R.string.word_traveler)).assertIsDisplayed()
    }

    @Test
    fun themeCards_reflectAndChangeTheSelection() {
        setScreen()

        composeRule.onNodeWithTag(SettingsTestTags.themeOption(AppTheme.LIGHT)).assertIsSelected()
        composeRule.onNodeWithTag(SettingsTestTags.themeOption(AppTheme.DARK)).assertIsNotSelected()

        composeRule.onNodeWithContentDescription(str(R.string.cd_theme_option, str(R.string.theme_dark))).performClick()

        assertThat(themeChanges).containsExactly(AppTheme.DARK)
        composeRule.onNodeWithTag(SettingsTestTags.themeOption(AppTheme.DARK)).assertIsSelected()
        composeRule.onNodeWithTag(SettingsTestTags.themeOption(AppTheme.LIGHT)).assertIsNotSelected()
    }

    @Test
    fun about_showsAppNameAndVersion() {
        setScreen()

        composeRule.onNodeWithText("Parley").assertIsDisplayed()
        composeRule.onNodeWithText("v1.0 (7)").assertIsDisplayed()
    }

    @Test
    fun backButton_navigatesBack() {
        setScreen()

        composeRule.onNodeWithContentDescription(str(R.string.cd_back_to_translator)).performClick()

        assertThat(backPressed).isTrue()
    }
}
