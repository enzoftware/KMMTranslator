package com.enzoftware.translatorapp.android.translate.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozyMotion
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.core.designsystem.cozyClickable

@Composable
fun SwapLanguageButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
) {
    val colors = CozyTheme.colors
    val haptics = LocalHapticFeedback.current
    var swapCount by remember { mutableIntStateOf(0) }
    val rotation by animateFloatAsState(
        targetValue = swapCount * 180f,
        animationSpec = CozyMotion.playful(),
        label = "swapRotation",
    )

    Box(
        modifier = modifier
            .cozyClickable(pressedScale = 0.9f) {
                swapCount++
                haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                onClick()
            }
            .size(size)
            .dropShadow(
                CircleShape,
                Shadow(radius = 8.dp, color = colors.indigo.copy(alpha = 0.3f), offset = DpOffset(0.dp, 4.dp)),
            )
            .clip(CircleShape)
            .background(colors.indigo),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.SwapHoriz,
            contentDescription = stringResource(R.string.swap_languages),
            tint = colors.onIndigo,
            modifier = Modifier
                .size(size * 0.5f)
                .rotate(rotation),
        )
    }
}
