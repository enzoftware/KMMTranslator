package com.enzoftware.translatorapp.android.voice_to_text.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozyMotion
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.core.designsystem.cozyClickable
import com.enzoftware.translatorapp.voice_to_text.presentation.DisplayState

/** Big round record button with breathing rings that react to the live mic level while speaking. */
@Composable
fun VoiceRecorderButton(
    displayState: DisplayState,
    livePower: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CozyTheme.colors
    val haptics = LocalHapticFeedback.current
    val isSpeaking = displayState == DisplayState.SPEAKING
    val buttonColor by animateColorAsState(
        when (displayState) {
            DisplayState.SPEAKING -> colors.terracotta
            DisplayState.DISPLAYING_RESULTS -> colors.sage
            else -> colors.indigo
        },
        CozyMotion.cozy(),
        label = "recorderColor",
    )
    val description = stringResource(
        when (displayState) {
            DisplayState.SPEAKING -> R.string.stop_recording
            DisplayState.DISPLAYING_RESULTS -> R.string.apply
            DisplayState.ERROR -> R.string.retry_recording
            else -> R.string.start_recording
        },
    )
    val breathing by rememberInfiniteTransition(label = "breathing").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath",
    )
    val power by animateFloatAsState(livePower.coerceIn(0f, 1f), CozyMotion.snappy(), label = "power")

    LaunchedEffect(displayState) {
        haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
    }

    Box(
        modifier = modifier
            .size(108.dp)
            .clip(CircleShape)
            .cozyClickable(pressedScale = 0.92f, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(visible = isSpeaking, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer {
                            val s = 1f + breathing * 0.18f + power * 0.22f
                            scaleX = s
                            scaleY = s
                        }
                        .clip(CircleShape)
                        .background(buttonColor.copy(alpha = 0.18f)),
                )
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .graphicsLayer {
                            val s = 0.98f + breathing * 0.14f
                            scaleX = s
                            scaleY = s
                        }
                        .border(2.dp, buttonColor.copy(alpha = 0.32f), CircleShape),
                )
            }
        }
        Box(
            modifier = Modifier
                .size(74.dp)
                .dropShadow(
                    CircleShape,
                    Shadow(radius = 14.dp, color = buttonColor.copy(alpha = 0.35f), offset = DpOffset(0.dp, 6.dp)),
                )
                .clip(CircleShape)
                .background(buttonColor),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = displayState,
                transitionSpec = { (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                label = "recorderIcon",
            ) { state ->
                Icon(
                    imageVector = when (state) {
                        DisplayState.SPEAKING -> Icons.Rounded.Stop
                        DisplayState.DISPLAYING_RESULTS -> Icons.Rounded.Check
                        else -> Icons.Rounded.Mic
                    },
                    contentDescription = null,
                    tint = colors.onIndigo,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}
