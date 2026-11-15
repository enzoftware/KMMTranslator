package com.enzoftware.translatorapp.android.voice_to_text.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enzoftware.translatorapp.android.core.designsystem.CozySpacing
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.core.designsystem.cozyCard
import kotlin.math.max
import kotlin.math.min

/** Live waveform: rounded bars with an indigo→terracotta gradient, newest on the right, tapered edge. */
@Composable
fun VoiceRecorderDisplay(
    powerRatios: List<Float>,
    modifier: Modifier = Modifier,
) {
    val colors = CozyTheme.colors
    Canvas(
        modifier = modifier
            .cozyCard(elevated = true)
            .padding(horizontal = CozySpacing.md, vertical = CozySpacing.sm),
    ) {
        val barWidth = 4.dp.toPx()
        val stride = barWidth + 4.dp.toPx()
        val barCount = max(1, (size.width / stride).toInt())
        val minLevel = 0.08f
        val normalized = powerRatios
            .map { it.coerceIn(minLevel, 1f) }
            .takeLast(barCount)
            .reversed()
        val brush = Brush.verticalGradient(listOf(colors.indigo, colors.terracotta))
        val centerY = size.height / 2f
        val maxHalfHeight = max(8.dp.toPx(), centerY - 12.dp.toPx())
        val taperSpan = max(barCount / 4, 1)

        clipRect {
            normalized.forEachIndexed { index, ratio ->
                val edgeDistance = min(1f, (barCount - index).toFloat() / taperSpan)
                val taperedRatio = ratio * (0.45f + 0.55f * edgeDistance)
                val halfHeight = max(3.dp.toPx(), maxHalfHeight * taperedRatio)
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(size.width - barWidth - index * stride, centerY - halfHeight),
                    size = Size(barWidth, halfHeight * 2f),
                    cornerRadius = CornerRadius(barWidth / 2f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun VoiceRecorderDisplayPreview() {
    CozyTheme(darkTheme = false) {
        VoiceRecorderDisplay(
            powerRatios = listOf(0.15f, 0.35f, 0.65f, 0.9f, 0.45f, 0.8f, 0.55f, 0.3f, 0.7f, 0.95f, 0.6f, 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        )
    }
}
