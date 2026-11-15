package com.enzoftware.translatorapp.android.translate.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozyIconCircle
import com.enzoftware.translatorapp.android.core.designsystem.CozyMotion
import com.enzoftware.translatorapp.android.core.designsystem.CozyRadius
import com.enzoftware.translatorapp.android.core.designsystem.CozySpacing
import com.enzoftware.translatorapp.android.core.designsystem.CozyText
import com.enzoftware.translatorapp.android.core.designsystem.CozyTextSize
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.core.designsystem.CozyCopyChip
import com.enzoftware.translatorapp.android.core.designsystem.CozySpeakButton
import com.enzoftware.translatorapp.android.core.designsystem.cozyCard
import com.enzoftware.translatorapp.android.core.designsystem.cozyClickable
import com.enzoftware.translatorapp.core.domain.language.Language
import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.translate.presentation.TranslateEvent

object TranslateTextFieldTestTags {
    const val EDITOR = "translate_editor"
    const val TRANSLATED_TEXT = "translated_text"
    const val TAP_TO_EDIT = "tap_to_edit"
}

val StarterIdioms = listOf(
    R.string.idiom_break_a_leg,
    R.string.idiom_spill_the_tea,
    R.string.idiom_piece_of_cake,
    R.string.idiom_under_the_weather,
)

/**
 * The main translator card. Shows an editor while idle/translating and the source/translation
 * pair once a result is available.
 */
@Composable
fun TranslateTextField(
    fromText: String,
    toText: String?,
    isTranslating: Boolean,
    fromLanguage: UiLanguage,
    toLanguage: UiLanguage,
    isSpeaking: Boolean,
    onTranslateEvent: (TranslateEvent) -> Unit,
    onSpeakClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isShowingResult = toText != null && !isTranslating
    AnimatedContent(
        targetState = isShowingResult,
        transitionSpec = {
            (fadeIn(CozyMotion.cozy()) + scaleIn(CozyMotion.cozy(), initialScale = 0.97f)) togetherWith
                fadeOut(CozyMotion.gentle())
        },
        modifier = modifier
            .fillMaxWidth()
            .cozyCard(elevated = true)
            .padding(CozySpacing.md),
        label = "translateCard",
    ) { showingResult ->
        if (showingResult) {
            TranslatedContent(
                fromText = fromText,
                toText = toText.orEmpty(),
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
                isSpeaking = isSpeaking,
                onTranslateEvent = onTranslateEvent,
                onSpeakClick = onSpeakClick,
            )
        } else {
            IdleContent(
                fromText = fromText,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
                isTranslating = isTranslating,
                onTranslateEvent = onTranslateEvent,
            )
        }
    }
}

@Composable
private fun IdleContent(
    fromText: String,
    fromLanguage: UiLanguage,
    toLanguage: UiLanguage,
    isTranslating: Boolean,
    onTranslateEvent: (TranslateEvent) -> Unit,
) {
    val colors = CozyTheme.colors
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        if (isFocused) colors.indigo.copy(alpha = 0.45f) else colors.borderSubtle.copy(alpha = 0.7f),
        CozyMotion.gentle(),
        label = "editorBorder",
    )
    val editorShape = RoundedCornerShape(CozyRadius.inner)

    Column(verticalArrangement = Arrangement.spacedBy(CozySpacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LanguageDisplay(language = fromLanguage)
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = colors.inkMuted,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = toLanguage.language.languageName,
                style = CozyText.sans(CozyTextSize.caption, FontWeight.SemiBold),
                color = colors.indigo,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.indigoSoft)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(
                visible = fromText.isNotEmpty() && !isTranslating,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut(),
            ) {
                Row(
                    modifier = Modifier
                        .cozyClickable { onTranslateEvent(TranslateEvent.CloseTranslation) }
                        .clip(CircleShape)
                        .background(colors.paperMuted.copy(alpha = 0.75f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Rounded.Cancel, contentDescription = null, tint = colors.inkSecondary, modifier = Modifier.size(13.dp))
                    Text(
                        stringResource(R.string.clear),
                        style = CozyText.sans(CozyTextSize.caption, FontWeight.Medium),
                        color = colors.inkSecondary,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(editorShape)
                .background(colors.paperMuted.copy(alpha = 0.32f))
                .border(1.dp, borderColor, editorShape)
                .padding(12.dp),
        ) {
            if (fromText.isEmpty()) {
                Text(
                    text = stringResource(R.string.enter_text),
                    style = CozyText.serif(CozyTextSize.body),
                    color = colors.inkMuted,
                )
            }
            BasicTextField(
                value = fromText,
                onValueChange = { onTranslateEvent(TranslateEvent.ChangeTranslationText(it)) },
                enabled = !isTranslating,
                textStyle = CozyText.sans(CozyTextSize.body).copy(color = colors.inkPrimary),
                cursorBrush = SolidColor(colors.indigo),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 108.dp, max = 176.dp)
                    .testTag(TranslateTextFieldTestTags.EDITOR)
                    .onFocusChanged { isFocused = it.isFocused },
            )
        }

        AnimatedVisibility(
            visible = fromText.isEmpty() && fromLanguage.language == Language.ENGLISH,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StarterIdioms.forEach { idiomRes ->
                    val phrase = stringResource(idiomRes)
                    Row(
                        modifier = Modifier
                            .cozyClickable { onTranslateEvent(TranslateEvent.ChangeTranslationText(phrase)) }
                            .clip(CircleShape)
                            .background(colors.paperMuted.copy(alpha = 0.65f))
                            .border(1.dp, colors.borderSubtle, CircleShape)
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(Icons.Rounded.FormatQuote, contentDescription = null, tint = colors.indigo, modifier = Modifier.size(12.dp))
                        Text(phrase, style = CozyText.sans(CozyTextSize.caption, FontWeight.Medium), color = colors.inkSecondary)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.chars_count, fromText.length),
                style = CozyText.sans(CozyTextSize.caption, FontWeight.Medium),
                color = colors.inkMuted,
                modifier = Modifier.weight(1f),
            )
            ProgressButton(
                text = stringResource(R.string.translate),
                isLoading = isTranslating,
                isDisabled = fromText.isBlank(),
                onClick = {
                    focusManager.clearFocus()
                    onTranslateEvent(TranslateEvent.Translate)
                },
            )
        }
    }
}

@Composable
private fun TranslatedContent(
    fromText: String,
    toText: String,
    fromLanguage: UiLanguage,
    toLanguage: UiLanguage,
    isSpeaking: Boolean,
    onTranslateEvent: (TranslateEvent) -> Unit,
    onSpeakClick: () -> Unit,
) {
    val colors = CozyTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(CozySpacing.md)) {
        Column(verticalArrangement = Arrangement.spacedBy(CozySpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageDisplay(language = fromLanguage)
                Spacer(Modifier.weight(1f))
                CozyCopyChip(text = fromText)
                CozyIconCircle(
                    icon = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.cd_close_translation),
                    onClick = { onTranslateEvent(TranslateEvent.CloseTranslation) },
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .cozyClickable(pressedScale = 0.98f) { onTranslateEvent(TranslateEvent.EditTranslation) }
                    .testTag(TranslateTextFieldTestTags.TAP_TO_EDIT)
                    .clip(RoundedCornerShape(CozyRadius.inner))
                    .background(colors.paperMuted.copy(alpha = 0.35f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(fromText, style = CozyText.sans(CozyTextSize.subheadline), color = colors.inkSecondary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = colors.inkMuted, modifier = Modifier.size(11.dp))
                    Text(
                        stringResource(R.string.tap_to_edit),
                        style = CozyText.sans(CozyTextSize.caption2, FontWeight.Medium),
                        color = colors.inkMuted,
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = colors.borderSubtle)
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = colors.indigo.copy(alpha = 0.75f),
                modifier = Modifier.size(12.dp),
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = colors.borderSubtle)
        }

        Column(verticalArrangement = Arrangement.spacedBy(CozySpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageDisplay(language = toLanguage)
                Spacer(Modifier.weight(1f))
                CozyCopyChip(text = toText)
                CozySpeakButton(isSpeaking = isSpeaking, onClick = onSpeakClick)
            }
            SelectionContainer {
                Text(
                    text = toText,
                    style = CozyText.serif(CozyTextSize.title2, FontWeight.Medium),
                    color = colors.inkPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag(TranslateTextFieldTestTags.TRANSLATED_TEXT),
                )
            }
        }
    }
}
