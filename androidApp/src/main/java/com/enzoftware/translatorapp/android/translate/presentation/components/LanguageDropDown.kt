package com.enzoftware.translatorapp.android.translate.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.enzoftware.translatorapp.android.core.designsystem.CozyMotion
import com.enzoftware.translatorapp.android.core.designsystem.CozyRadius
import com.enzoftware.translatorapp.android.core.designsystem.CozyText
import com.enzoftware.translatorapp.android.core.designsystem.CozyTextSize
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.core.designsystem.cozyClickable
import com.enzoftware.translatorapp.core.presentation.UiLanguage

object LanguageDropDownTestTags {
    fun option(language: UiLanguage) = "language_option_${language.language.code}"
}

/** Language selector pill with a themed menu, used inside the bottom command dock. */
@Composable
fun LanguageDropDown(
    language: UiLanguage,
    isOpen: Boolean,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    onLanguageSelected: (UiLanguage) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
) {
    val colors = CozyTheme.colors
    val haptics = LocalHapticFeedback.current
    val chevronRotation by animateFloatAsState(
        targetValue = if (isOpen) 180f else 0f,
        animationSpec = CozyMotion.snappy(),
        label = "chevron",
    )
    var lastCode by remember { mutableStateOf(language.language.code) }
    LaunchedEffect(language.language.code) {
        if (lastCode != language.language.code) {
            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
            lastCode = language.language.code
        }
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .cozyClickable(onClick = onClick)
                .semantics(mergeDescendants = true) { this.contentDescription = contentDescription }
                .clip(CircleShape)
                .background(colors.paperMuted.copy(alpha = 0.65f))
                .padding(horizontal = if (compact) 9.dp else 12.dp, vertical = if (compact) 7.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp, Alignment.CenterHorizontally),
        ) {
            SmallLanguageIcon(language = language, size = if (compact) 20.dp else 24.dp)
            AnimatedContent(
                targetState = language,
                transitionSpec = { fadeIn(CozyMotion.snappy()) togetherWith fadeOut(CozyMotion.snappy()) },
                modifier = Modifier.weight(1f, fill = false),
                label = "languageName",
            ) { current ->
                Text(
                    text = current.language.languageName,
                    style = CozyText.sans(
                        if (compact) CozyTextSize.footnote else CozyTextSize.subheadline,
                        FontWeight.SemiBold,
                    ),
                    color = colors.inkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.inkSecondary,
                modifier = Modifier
                    .size(14.dp)
                    .rotate(chevronRotation),
            )
        }

        DropdownMenu(
            expanded = isOpen,
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(CozyRadius.inner),
            containerColor = colors.paperElevated,
            border = BorderStroke(1.dp, colors.borderSubtle),
            shadowElevation = 8.dp,
        ) {
            UiLanguage.allLanguages.forEach { option ->
                val isSelected = option.language == language.language
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.language.languageName,
                            style = CozyText.sans(
                                CozyTextSize.subheadline,
                                if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            ),
                            color = colors.inkPrimary,
                        )
                    },
                    leadingIcon = { SmallLanguageIcon(language = option, size = 24.dp) },
                    trailingIcon = if (isSelected) {
                        { Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.indigo) }
                    } else {
                        null
                    },
                    onClick = { onLanguageSelected(option) },
                    modifier = Modifier.testTag(LanguageDropDownTestTags.option(option)),
                )
            }
        }
    }
}
