package com.enzoftware.translatorapp.android.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Paper card: warm two-layer shadow, subtle border, card or elevated fill. Mirrors iOS `cozyCard`. */
fun Modifier.cozyCard(
    cornerRadius: Dp = CozyRadius.card,
    elevated: Boolean = false,
): Modifier = composed {
    val colors = CozyTheme.colors
    val shape = RoundedCornerShape(cornerRadius)
    this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = if (elevated) 18.dp else 10.dp,
                color = colors.warmShadow.copy(alpha = if (colors.isDark) 0.36f else 0.07f),
                offset = DpOffset(0.dp, if (elevated) 8.dp else 4.dp),
            ),
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 2.dp,
                color = colors.warmShadow.copy(alpha = if (colors.isDark) 0.18f else 0.03f),
                offset = DpOffset(0.dp, 1.dp),
            ),
        )
        .clip(shape)
        .background(if (elevated) colors.paperElevated else colors.paperCard)
        .border(1.dp, colors.borderSubtle.copy(alpha = if (colors.isDark) 0.75f else 0.9f), shape)
}

/**
 * Floating translucent chrome used for docks and round toolbar buttons. iOS uses Liquid Glass
 * here; Android can't blur what's behind a view, so the fill is kept mostly opaque for legibility.
 */
fun Modifier.cozyFloatingChrome(
    shape: Shape,
    tint: Color? = null,
): Modifier = composed {
    val colors = CozyTheme.colors
    val fill = tint?.copy(alpha = 0.96f)
        ?: colors.paperCard.copy(alpha = if (colors.isDark) 0.84f else 0.88f)
    this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 14.dp,
                color = colors.warmShadow.copy(alpha = if (colors.isDark) 0.35f else 0.1f),
                offset = DpOffset(0.dp, 6.dp),
            ),
        )
        .clip(shape)
        .background(fill)
        .border(1.dp, colors.borderSubtle.copy(alpha = if (colors.isDark) 0.8f else 0.95f), shape)
}

/**
 * Click handling with the iOS `CozyScaleButtonStyle` feel: no ripple, a springy shrink and a slight
 * fade while pressed. Apply it first in the chain so the whole element scales.
 */
fun Modifier.cozyClickable(
    pressedScale: Float = 0.96f,
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = CozyMotion.snappy(),
        label = "pressScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = CozyMotion.snappy(),
        label = "pressAlpha",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = role,
            onClickLabel = onClickLabel,
            onClick = onClick,
        )
}

/** Fade-and-rise entrance on first composition, like iOS `cozyEntrance`. */
fun Modifier.cozyEntrance(
    delayMillis: Int = 0,
    offsetY: Dp = 14.dp,
): Modifier = composed {
    val progress = remember { Animatable(0f) }
    val offsetPx = with(LocalDensity.current) { offsetY.toPx() }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        progress.animateTo(1f, CozyMotion.cozy())
    }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * offsetPx
    }
}
