package com.enzoftware.translatorapp.android.core.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.max

/**
 * Warm paper backdrop with a slowly drifting colour mesh. iOS uses a 3x3 `MeshGradient`; here each
 * mesh control point becomes a soft radial glow over the paper colour, drifting over 9 seconds.
 * Compose honours the system animator scale, so "Remove animations" freezes the drift.
 */
@Composable
fun CozyAmbientBackground(modifier: Modifier = Modifier) {
    val colors = CozyTheme.colors
    val drift by rememberInfiniteTransition(label = "meshDrift").animateFloat(
        initialValue = -0.06f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "meshShift",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                0f to colors.meshTopCenter,
                0.5f to colors.meshMidCenter,
                1f to colors.paperBackground,
            ),
        )
        val glowAlpha = if (colors.isDark) 0.92f else 1f
        glow(colors.meshTopLeading, Offset(0f, 0f), 0.7f, glowAlpha)
        glow(colors.meshTopTrailing, Offset(size.width, 0f), 0.75f, glowAlpha)
        glow(colors.meshBottomCool, Offset(size.width, size.height * (0.52f + drift * 0.4f)), 0.6f, glowAlpha)
        glow(colors.meshBottomWarm, Offset(0f, size.height), 0.8f, glowAlpha)
        glow(colors.meshBottomCool, Offset(size.width, size.height), 0.7f, glowAlpha)
        glow(
            colors.meshMidCenter,
            Offset(size.width * (0.52f + drift), size.height * (0.46f - drift * 0.6f)),
            0.45f,
            glowAlpha,
        )
    }
}

private fun DrawScope.glow(color: Color, center: Offset, radiusFraction: Float, alpha: Float) {
    val radius = max(size.width, size.height) * radiusFraction
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

@Preview
@Composable
private fun CozyAmbientBackgroundPreview() {
    CozyTheme(darkTheme = false) { CozyAmbientBackground() }
}
