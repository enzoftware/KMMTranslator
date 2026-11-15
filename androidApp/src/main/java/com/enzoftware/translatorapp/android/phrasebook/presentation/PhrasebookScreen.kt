package com.enzoftware.translatorapp.android.phrasebook.presentation

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozyAmbientBackground
import com.enzoftware.translatorapp.android.core.designsystem.CozyChromeIconButton
import com.enzoftware.translatorapp.android.core.designsystem.CozyIconCircle
import com.enzoftware.translatorapp.android.core.designsystem.CozyMotion
import com.enzoftware.translatorapp.android.core.designsystem.CozySpacing
import com.enzoftware.translatorapp.android.core.designsystem.CozyText
import com.enzoftware.translatorapp.android.core.designsystem.CozyTextSize
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.core.designsystem.cozyCard
import com.enzoftware.translatorapp.android.core.designsystem.cozyClickable
import com.enzoftware.translatorapp.android.core.designsystem.cozyFloatingChrome
import com.enzoftware.translatorapp.android.translate.presentation.components.SmallLanguageIcon
import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.phrasebook.domain.PhrasebookCategory
import com.enzoftware.translatorapp.phrasebook.presentation.PhrasebookEvent
import com.enzoftware.translatorapp.phrasebook.presentation.PhrasebookState
import com.enzoftware.translatorapp.phrasebook.presentation.UiPhrase
import com.enzoftware.translatorapp.phrasebook.presentation.UiSavedWord

object PhrasebookTestTags {
    const val TAB_SUGGESTED = "phrasebook_tab_suggested"
    const val TAB_MY_WORDS = "phrasebook_tab_my_words"
    fun phrase(entryKey: String) = "phrase_$entryKey"
    fun savedWord(word: UiSavedWord) = "saved_${word.entryKey}_${word.fromLanguage.language.code}_${word.toLanguage.language.code}"
}

enum class PhrasebookTab { SUGGESTED, MY_WORDS }

@StringRes
fun PhrasebookCategory.labelRes(): Int = when (this) {
    PhrasebookCategory.GREETINGS -> R.string.category_greetings
    PhrasebookCategory.COURTESY -> R.string.category_courtesy
    PhrasebookCategory.CONVERSATION -> R.string.category_conversation
    PhrasebookCategory.TRAVEL -> R.string.category_travel
    PhrasebookCategory.FOOD -> R.string.category_food
}

/**
 * Offline phrasebook: built-in suggestions for the current language pair that can be saved to
 * "My Words" (and removed again), plus the saved list across all pairs.
 */
@Composable
fun PhrasebookScreen(
    state: PhrasebookState,
    onEvent: (PhrasebookEvent) -> Unit,
    onUsePhrase: (entryKey: String, fromLanguageCode: String?, toLanguageCode: String?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableStateOf(PhrasebookTab.SUGGESTED) }
    val navBars = WindowInsets.navigationBars.asPaddingValues()

    Box(modifier = modifier.fillMaxSize()) {
        CozyAmbientBackground()
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Column(
                modifier = Modifier.padding(start = CozySpacing.md, end = CozySpacing.md, top = CozySpacing.sm),
                verticalArrangement = Arrangement.spacedBy(CozySpacing.md),
            ) {
                TopBar(state.fromLanguage, state.toLanguage, onBack)
                TabSwitch(selected = tab, savedCount = state.savedWords.size, onSelect = { tab = it })
            }
            when (tab) {
                PhrasebookTab.SUGGESTED -> SuggestedList(
                    state = state,
                    onEvent = onEvent,
                    onUse = { onUsePhrase(it, null, null) },
                    bottomPadding = navBars.calculateBottomPadding(),
                )

                PhrasebookTab.MY_WORDS -> MyWordsList(
                    words = state.savedWords,
                    onRemove = { word ->
                        onEvent(
                            PhrasebookEvent.RemoveSavedWord(
                                word.entryKey,
                                word.fromLanguage.language.code,
                                word.toLanguage.language.code,
                            ),
                        )
                    },
                    onUse = { word ->
                        onUsePhrase(word.entryKey, word.fromLanguage.language.code, word.toLanguage.language.code)
                    },
                    bottomPadding = navBars.calculateBottomPadding(),
                )
            }
        }
    }
}

@Composable
private fun TopBar(from: UiLanguage, to: UiLanguage, onBack: () -> Unit) {
    val colors = CozyTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        CozyChromeIconButton(
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.cd_back_to_translator),
            onClick = onBack,
        )
        Spacer(Modifier.size(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.phrasebook), style = CozyText.rounded(CozyTextSize.title2), color = colors.inkPrimary)
            Text(
                stringResource(R.string.phrasebook_subtitle, from.language.languageName, to.language.languageName),
                style = CozyText.sans(CozyTextSize.caption),
                color = colors.inkSecondary,
            )
        }
    }
}

@Composable
private fun TabSwitch(selected: PhrasebookTab, savedCount: Int, onSelect: (PhrasebookTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cozyFloatingChrome(CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TabButton(
            text = stringResource(R.string.tab_suggested),
            isSelected = selected == PhrasebookTab.SUGGESTED,
            onClick = { onSelect(PhrasebookTab.SUGGESTED) },
            modifier = Modifier.weight(1f).testTag(PhrasebookTestTags.TAB_SUGGESTED),
        )
        TabButton(
            text = if (savedCount > 0) stringResource(R.string.tab_my_words_count, savedCount) else stringResource(R.string.tab_my_words),
            isSelected = selected == PhrasebookTab.MY_WORDS,
            onClick = { onSelect(PhrasebookTab.MY_WORDS) },
            modifier = Modifier.weight(1f).testTag(PhrasebookTestTags.TAB_MY_WORDS),
        )
    }
}

@Composable
private fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CozyTheme.colors
    val container by animateColorAsState(if (isSelected) colors.indigo else colors.indigo.copy(alpha = 0f), CozyMotion.snappy(), label = "tab")
    val content by animateColorAsState(if (isSelected) colors.onIndigo else colors.inkSecondary, CozyMotion.snappy(), label = "tabText")
    Box(
        modifier = modifier
            .cozyClickable(role = Role.Tab, onClick = onClick)
            .semantics { selected = isSelected }
            .clip(CircleShape)
            .background(container)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = CozyText.sans(CozyTextSize.subheadline, FontWeight.SemiBold), color = content)
    }
}

@Composable
private fun SuggestedList(
    state: PhrasebookState,
    onEvent: (PhrasebookEvent) -> Unit,
    onUse: (String) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = CozySpacing.md, vertical = CozySpacing.md),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                CategoryChip(stringResource(R.string.category_all), state.selectedCategory == null) {
                    onEvent(PhrasebookEvent.SelectCategory(null))
                }
            }
            items(PhrasebookCategory.entries) { category ->
                CategoryChip(stringResource(category.labelRes()), state.selectedCategory == category) {
                    onEvent(PhrasebookEvent.SelectCategory(category))
                }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = CozySpacing.md, end = CozySpacing.md, bottom = bottomPadding + CozySpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CozySpacing.sm),
        ) {
            items(state.suggestions, key = { it.entryKey }) { phrase ->
                PhraseCard(
                    phrase = phrase,
                    onToggleSaved = { onEvent(PhrasebookEvent.ToggleSaved(phrase.entryKey)) },
                    onUse = { onUse(phrase.entryKey) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = CozyTheme.colors
    Text(
        text = text,
        style = CozyText.sans(CozyTextSize.caption, FontWeight.SemiBold),
        color = if (isSelected) colors.indigo else colors.inkSecondary,
        modifier = Modifier
            .cozyClickable(role = Role.Tab, onClick = onClick)
            .semantics { selected = isSelected }
            .clip(CircleShape)
            .background(if (isSelected) colors.indigoSoft else colors.paperMuted.copy(alpha = 0.65f))
            .border(1.dp, if (isSelected) colors.indigo.copy(alpha = 0.4f) else colors.borderSubtle, CircleShape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

@Composable
private fun PhraseCard(phrase: UiPhrase, onToggleSaved: () -> Unit, onUse: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CozyTheme.colors
    val haptics = LocalHapticFeedback.current
    val toggleDescription = stringResource(
        if (phrase.isSaved) R.string.cd_unsave_word else R.string.cd_save_word,
        phrase.fromText,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .cozyClickable(pressedScale = 0.98f, onClickLabel = stringResource(R.string.tap_to_use), onClick = onUse)
            .testTag(PhrasebookTestTags.phrase(phrase.entryKey))
            .cozyCard(cornerRadius = 20.dp)
            .padding(start = CozySpacing.md, top = 12.dp, bottom = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(phrase.toText, style = CozyText.serif(CozyTextSize.headline, FontWeight.SemiBold), color = colors.inkPrimary)
            Text(phrase.fromText, style = CozyText.sans(CozyTextSize.caption), color = colors.inkSecondary)
        }
        Box(
            modifier = Modifier
                .cozyClickable(pressedScale = 0.9f) {
                    haptics.performHapticFeedback(if (phrase.isSaved) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn)
                    onToggleSaved()
                }
                .semantics { contentDescription = toggleDescription }
                .size(40.dp)
                .clip(CircleShape)
                .background(if (phrase.isSaved) colors.indigoSoft else colors.paperMuted.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (phrase.isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                contentDescription = null,
                tint = if (phrase.isSaved) colors.indigo else colors.inkSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun MyWordsList(
    words: List<UiSavedWord>,
    onRemove: (UiSavedWord) -> Unit,
    onUse: (UiSavedWord) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
) {
    if (words.isEmpty()) {
        EmptyMyWords(Modifier.padding(CozySpacing.md))
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = CozySpacing.md, end = CozySpacing.md, top = CozySpacing.md, bottom = bottomPadding + CozySpacing.lg),
        verticalArrangement = Arrangement.spacedBy(CozySpacing.sm),
    ) {
        items(words, key = { PhrasebookTestTags.savedWord(it) }) { word ->
            val dismissState = rememberSwipeToDismissBoxState()
            LaunchedEffect(dismissState.currentValue) {
                if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) onRemove(word)
            }
            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = { RemoveBackground() },
                modifier = Modifier.animateItem(),
            ) {
                SavedWordCard(word = word, onRemove = { onRemove(word) }, onUse = { onUse(word) })
            }
        }
    }
}

@Composable
private fun RemoveBackground() {
    val colors = CozyTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.danger.copy(alpha = 0.14f))
            .padding(horizontal = CozySpacing.lg),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(Icons.Rounded.Delete, contentDescription = null, tint = colors.danger)
    }
}

@Composable
private fun SavedWordCard(word: UiSavedWord, onRemove: () -> Unit, onUse: () -> Unit) {
    val colors = CozyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cozyClickable(pressedScale = 0.98f, onClickLabel = stringResource(R.string.tap_to_use), onClick = onUse)
            .testTag(PhrasebookTestTags.savedWord(word))
            .cozyCard(cornerRadius = 20.dp)
            .padding(start = CozySpacing.md, top = 12.dp, bottom = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                SmallLanguageIcon(word.fromLanguage, size = 16.dp)
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = colors.inkMuted, modifier = Modifier.size(10.dp))
                SmallLanguageIcon(word.toLanguage, size = 16.dp)
            }
            Text(word.toText, style = CozyText.serif(CozyTextSize.headline, FontWeight.SemiBold), color = colors.inkPrimary)
            Text(word.fromText, style = CozyText.sans(CozyTextSize.caption), color = colors.inkSecondary)
        }
        CozyIconCircle(
            icon = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.cd_remove_saved_word, word.fromText),
            onClick = onRemove,
        )
    }
}

@Composable
private fun EmptyMyWords(modifier: Modifier = Modifier) {
    val colors = CozyTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .cozyCard(cornerRadius = 22.dp)
            .padding(CozySpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(colors.indigoSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Bookmarks, contentDescription = null, tint = colors.indigo, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.size(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(stringResource(R.string.my_words_empty_title), style = CozyText.sans(CozyTextSize.subheadline, FontWeight.SemiBold), color = colors.inkPrimary)
            Text(stringResource(R.string.my_words_empty_message), style = CozyText.sans(CozyTextSize.caption), color = colors.inkSecondary)
        }
    }
}

@Preview
@Composable
private fun PhrasebookScreenPreview() {
    CozyTheme(darkTheme = false) {
        PhrasebookScreen(
            state = PhrasebookState(
                suggestions = listOf(
                    UiPhrase("thank_you", PhrasebookCategory.COURTESY, "Thank you", "Gracias", isSaved = true),
                    UiPhrase("water", PhrasebookCategory.FOOD, "Water", "Agua", isSaved = false),
                ),
            ),
            onEvent = {},
            onUsePhrase = { _, _, _ -> },
            onBack = {},
        )
    }
}
