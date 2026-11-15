package com.enzoftware.translatorapp.android.translate.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozyMotion
import com.enzoftware.translatorapp.android.core.designsystem.CozyText
import com.enzoftware.translatorapp.android.core.designsystem.CozyTextSize
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.core.designsystem.cozyClickable

/** Indigo "Translate ✨" capsule that turns into a "Translating" spinner. */
@Composable
fun ProgressButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDisabled: Boolean = false,
) {
    val colors = CozyTheme.colors
    val haptics = LocalHapticFeedback.current
    val enabled = !isDisabled && !isLoading
    val container by animateColorAsState(
        if (isDisabled && !isLoading) colors.indigo.copy(alpha = 0.45f) else colors.indigo,
        CozyMotion.gentle(),
        label = "progressContainer",
    )
    LaunchedEffect(isLoading) {
        if (isLoading) haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
    }

    Row(
        modifier = modifier
            .cozyClickable(enabled = enabled, onClick = onClick)
            .dropShadow(
                CircleShape,
                Shadow(
                    radius = 10.dp,
                    color = colors.indigo.copy(alpha = if (isDisabled) 0.08f else 0.28f),
                    offset = DpOffset(0.dp, 4.dp),
                ),
            )
            .clip(CircleShape)
            .background(container)
            .animateContentSize(CozyMotion.snappy())
            .padding(horizontal = if (isLoading) 16.dp else 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = { fadeIn(CozyMotion.snappy()) togetherWith fadeOut(CozyMotion.snappy()) },
            label = "progressContent",
        ) { loading ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = colors.onIndigo,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        stringResource(R.string.translating),
                        style = CozyText.sans(CozyTextSize.subheadline, FontWeight.SemiBold),
                        color = colors.onIndigo,
                    )
                } else {
                    Text(
                        text,
                        style = CozyText.sans(CozyTextSize.subheadline, FontWeight.SemiBold),
                        color = colors.onIndigo,
                    )
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = colors.onIndigo,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}
