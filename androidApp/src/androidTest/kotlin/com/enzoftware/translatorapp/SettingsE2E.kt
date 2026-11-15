package com.enzoftware.translatorapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.enzoftware.translatorapp.android.MainActivity
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.di.AppModule
import com.enzoftware.translatorapp.android.settings.di.SettingsModule
import com.enzoftware.translatorapp.android.settings.domain.AppTheme
import com.enzoftware.translatorapp.android.settings.domain.SettingsRepository
import com.enzoftware.translatorapp.android.settings.presentation.SettingsTestTags
import com.enzoftware.translatorapp.android.translate.presentation.TranslateScreenTestTags
import com.enzoftware.translatorapp.android.voice_to_text.di.VoiceToTextModule
import com.enzoftware.translatorapp.settings.FakeSettingsRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class, VoiceToTextModule::class, SettingsModule::class)
class SettingsE2E {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var repository: SettingsRepository

    private val settings get() = (repository as FakeSettingsRepository).settings.value

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun openSettings() {
        composeRule.awaitDescription(str(R.string.cd_open_settings)).performClick()
        composeRule.awaitText(str(R.string.settings_subtitle))
    }

    private fun goBack() {
        composeRule.onNodeWithContentDescription(str(R.string.cd_back_to_translator)).performClick()
        composeRule.awaitTag(TranslateScreenTestTags.GREETING_TITLE)
    }

    @Test
    fun settings_showsProfileAppearanceAndAbout() {
        openSettings()

        composeRule.onNodeWithText(str(R.string.word_traveler)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.appearance)).assertIsDisplayed()
        composeRule.onNodeWithTag(SettingsTestTags.themeOption(AppTheme.LIGHT)).assertIsSelected()
        composeRule.onNodeWithText(str(R.string.app_name)).assertIsDisplayed()
    }

    @Test
    fun enteringAName_personalizesTheHomeGreeting() {
        openSettings()
        composeRule.onNodeWithTag(SettingsTestTags.NAME_FIELD).performTextInput("Ada Lovelace")
        composeRule.awaitText(str(R.string.hi_name_exclaim, "Ada Lovelace"))

        goBack()

        composeRule.awaitText(str(R.string.hi_name, "Ada Lovelace"))
        composeRule.onNodeWithTag(TranslateScreenTestTags.GREETING_TITLE)
            .assertTextEquals(str(R.string.hi_name, "Ada Lovelace"))
        composeRule.onNodeWithText("AL").assertIsDisplayed()
        assertThat(settings.userName).isEqualTo("Ada Lovelace")
    }

    @Test
    fun choosingDarkTheme_isSavedAndRemembered() {
        openSettings()
        composeRule.onNodeWithTag(SettingsTestTags.themeOption(AppTheme.DARK)).performClick()

        composeRule.waitUntil(5_000) { settings.theme == AppTheme.DARK }
        goBack()
        openSettings()

        composeRule.onNodeWithTag(SettingsTestTags.themeOption(AppTheme.DARK)).assertIsSelected()
    }
}
