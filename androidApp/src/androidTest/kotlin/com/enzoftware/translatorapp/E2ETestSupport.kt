package com.enzoftware.translatorapp

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.translate.presentation.TranslateScreenTestTags
import com.enzoftware.translatorapp.android.translate.presentation.components.LanguageDropDownTestTags
import com.enzoftware.translatorapp.android.translate.presentation.components.TranslateTextFieldTestTags
import com.enzoftware.translatorapp.core.presentation.UiLanguage

typealias AppComposeRule<A> = AndroidComposeTestRule<ActivityScenarioRule<A>, A>

internal val context: Context get() = ApplicationProvider.getApplicationContext()
internal fun str(id: Int, vararg args: Any): String = context.getString(id, *args)

/** Waits for a node to appear; screen transitions and entrance animations take a moment. */
internal fun <A : ComponentActivity> AppComposeRule<A>.awaitNode(
    matcher: androidx.compose.ui.test.SemanticsMatcher,
    timeoutMillis: Long = 5_000,
): SemanticsNodeInteraction {
    waitUntil(timeoutMillis) { onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    return onNode(matcher)
}

internal fun <A : ComponentActivity> AppComposeRule<A>.awaitText(text: String) = awaitNode(hasText(text))
internal fun <A : ComponentActivity> AppComposeRule<A>.awaitTag(tag: String) = awaitNode(hasTestTag(tag))
internal fun <A : ComponentActivity> AppComposeRule<A>.awaitDescription(description: String) =
    awaitNode(hasContentDescription(description))

internal fun <A : ComponentActivity> AppComposeRule<A>.awaitGone(text: String, timeoutMillis: Long = 5_000) {
    waitUntil(timeoutMillis) { onAllNodes(hasText(text)).fetchSemanticsNodes().isEmpty() }
}

// ─── Translate screen actions ─────────────────────────────────────────────────

internal fun <A : ComponentActivity> AppComposeRule<A>.typeSource(text: String) {
    awaitTag(TranslateTextFieldTestTags.EDITOR).performTextInput(text)
}

internal fun <A : ComponentActivity> AppComposeRule<A>.clickTranslate() {
    onNodeWithText(str(R.string.translate)).performClick()
}

internal fun <A : ComponentActivity> AppComposeRule<A>.closeTranslation() {
    onNodeWithContentDescription(str(R.string.cd_close_translation)).performClick()
}

internal fun <A : ComponentActivity> AppComposeRule<A>.chooseFromLanguage(code: String) {
    onNodeWithTag(TranslateScreenTestTags.FROM_LANGUAGE).performClick()
    awaitTag(LanguageDropDownTestTags.option(UiLanguage.byCode(code))).performClick()
}

internal fun <A : ComponentActivity> AppComposeRule<A>.chooseToLanguage(code: String) {
    onNodeWithTag(TranslateScreenTestTags.TO_LANGUAGE).performClick()
    awaitTag(LanguageDropDownTestTags.option(UiLanguage.byCode(code))).performClick()
}

internal fun <A : ComponentActivity> AppComposeRule<A>.openVoiceScreen() {
    awaitDescription(str(R.string.open_voice_translator)).performClick()
    awaitText(str(R.string.voice_ready_title))
}
