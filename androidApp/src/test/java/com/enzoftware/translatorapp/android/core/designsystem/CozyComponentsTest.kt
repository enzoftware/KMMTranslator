package com.enzoftware.translatorapp.android.core.designsystem

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.enzoftware.translatorapp.android.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CozyComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun copyChip_copiesAndShowsConfirmationThenResets() {
        val copied = mutableListOf<String>()
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            CozyTheme(darkTheme = false) {
                CozyCopyChip(text = "Hola", onCopy = { copied += it })
            }
        }
        composeRule.mainClock.advanceTimeBy(100)

        composeRule.onNodeWithContentDescription(context.getString(R.string.cd_copy_text)).performClick()
        composeRule.mainClock.advanceTimeBy(100)

        assertThat(copied).containsExactly("Hola")
        composeRule.onNodeWithText(context.getString(R.string.copied)).assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.onNodeWithText(context.getString(R.string.copy)).assertIsDisplayed()
    }

    @Test
    fun speakButton_reflectsSpeakingState() {
        var speaking by mutableStateOf(false)
        var clicks = 0
        composeRule.setContent {
            CozyTheme(darkTheme = true) {
                CozySpeakButton(isSpeaking = speaking, onClick = { clicks++; speaking = !speaking })
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.listen)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.cd_speak_translation)).performClick()

        assertThat(clicks).isEqualTo(1)
        composeRule.onNodeWithText(context.getString(R.string.speaking)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.cd_stop_speaking)).assertIsDisplayed()
    }
}
