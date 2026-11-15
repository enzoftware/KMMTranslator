package com.enzoftware.translatorapp.android.core.designsystem

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enzoftware.translatorapp.android.R
import kotlin.math.PI
import kotlin.math.pow

/**
 * "Warm Paper & Indigo" design tokens, mirrored from the iOS `CozyTheme`.
 */
@Immutable
data class CozyColors(
    val isDark: Boolean,
    val paperBackground: Color,
    val paperCard: Color,
    val paperElevated: Color,
    val paperMuted: Color,
    val indigo: Color,
    val indigoSoft: Color,
    val onIndigo: Color,
    val terracotta: Color,
    val sage: Color,
    val danger: Color,
    val inkPrimary: Color,
    val inkSecondary: Color,
    val inkMuted: Color,
    val borderSubtle: Color,
    val warmShadow: Color,
    val meshTopLeading: Color,
    val meshTopCenter: Color,
    val meshTopTrailing: Color,
    val meshMidCenter: Color,
    val meshBottomWarm: Color,
    val meshBottomCool: Color,
)

val LightCozyColors = CozyColors(
    isDark = false,
    paperBackground = Color(0xFFFAF5EC),
    paperCard = Color(0xFFFFFDF8),
    paperElevated = Color(0xFFFFFFFF),
    paperMuted = Color(0xFFF1E9DA),
    indigo = Color(0xFF4B4DED),
    indigoSoft = Color(0xFFEAE9FD),
    onIndigo = Color(0xFFFFFDF8),
    terracotta = Color(0xFFDE6E52),
    sage = Color(0xFF3B8666),
    danger = Color(0xFFC94A38),
    inkPrimary = Color(0xFF221D1A),
    inkSecondary = Color(0xFF6B6058),
    inkMuted = Color(0xFF988B81),
    borderSubtle = Color(0xFFE6DCCB),
    warmShadow = Color(0xFF2A1F14),
    meshTopLeading = Color(0xFFFDF7EB),
    meshTopCenter = Color(0xFFF6EDDF),
    meshTopTrailing = Color(0xFFEEE8F7),
    meshMidCenter = Color(0xFFFAF3E8),
    meshBottomWarm = Color(0xFFF7E6D8),
    meshBottomCool = Color(0xFFE9E7F6),
)

val DarkCozyColors = CozyColors(
    isDark = true,
    paperBackground = Color(0xFF171412),
    paperCard = Color(0xFF221E1B),
    paperElevated = Color(0xFF2B2622),
    paperMuted = Color(0xFF2F2A25),
    indigo = Color(0xFF7D80F9),
    indigoSoft = Color(0xFF2B2A4A),
    onIndigo = Color(0xFF13111C),
    terracotta = Color(0xFFF08A70),
    sage = Color(0xFF5AB88E),
    danger = Color(0xFFEB6B59),
    inkPrimary = Color(0xFFF6EFE6),
    inkSecondary = Color(0xFFB8ACA1),
    inkMuted = Color(0xFF83776C),
    borderSubtle = Color(0xFF36302B),
    warmShadow = Color(0xFF000000),
    meshTopLeading = Color(0xFF191513),
    meshTopCenter = Color(0xFF1F1B24),
    meshTopTrailing = Color(0xFF1C1A28),
    meshMidCenter = Color(0xFF1E1917),
    meshBottomWarm = Color(0xFF231B18),
    meshBottomCool = Color(0xFF181722),
)

object CozyRadius {
    val card: Dp = 24.dp
    val inner: Dp = 16.dp
    val badge: Dp = 12.dp
}

object CozySpacing {
    val xs: Dp = 6.dp
    val sm: Dp = 10.dp
    val md: Dp = 16.dp
    val lg: Dp = 22.dp
    val xl: Dp = 30.dp
}

object CozyMotion {
    /** Converts a SwiftUI `spring(response:dampingFraction:)` into Compose spring parameters. */
    private fun <T> iosSpring(response: Float, damping: Float): FiniteAnimationSpec<T> =
        spring(dampingRatio = damping, stiffness = (2 * PI / response).pow(2).toFloat())

    fun <T> snappy(): FiniteAnimationSpec<T> = iosSpring(response = 0.34f, damping = 0.78f)
    fun <T> cozy(): FiniteAnimationSpec<T> = iosSpring(response = 0.48f, damping = 0.82f)
    fun <T> playful(): FiniteAnimationSpec<T> = iosSpring(response = 0.42f, damping = 0.66f)
    fun <T> gentle(): FiniteAnimationSpec<T> = tween(durationMillis = 280, easing = FastOutSlowInEasing)

    /** Stagger delay for list entrances, capped after 8 items like iOS. */
    fun staggeredDelayMillis(index: Int): Int = index.coerceIn(0, 8) * 45
}

object CozyFonts {
    val rounded = FontFamily(
        Font(R.font.nunito_500, FontWeight.Medium),
        Font(R.font.nunito_600, FontWeight.SemiBold),
        Font(R.font.nunito_700, FontWeight.Bold),
        Font(R.font.nunito_800, FontWeight.ExtraBold),
    )
    val serif = FontFamily(
        Font(R.font.source_serif_400, FontWeight.Normal),
        Font(R.font.source_serif_500, FontWeight.Medium),
        Font(R.font.source_serif_600, FontWeight.SemiBold),
    )
}

/** iOS dynamic type sizes at the default content size category. */
object CozyTextSize {
    val caption2: TextUnit = 11.sp
    val caption: TextUnit = 12.sp
    val footnote: TextUnit = 13.sp
    val subheadline: TextUnit = 15.sp
    val body: TextUnit = 17.sp
    val headline: TextUnit = 17.sp
    val title3: TextUnit = 20.sp
    val title2: TextUnit = 22.sp
}

object CozyText {
    fun sans(size: TextUnit, weight: FontWeight = FontWeight.Normal) =
        TextStyle(fontSize = size, fontWeight = weight)

    fun rounded(size: TextUnit, weight: FontWeight = FontWeight.Bold) =
        TextStyle(fontFamily = CozyFonts.rounded, fontSize = size, fontWeight = weight)

    fun serif(size: TextUnit, weight: FontWeight = FontWeight.Normal) =
        TextStyle(fontFamily = CozyFonts.serif, fontSize = size, fontWeight = weight)
}

private val LocalCozyColors = staticCompositionLocalOf { LightCozyColors }

object CozyTheme {
    val colors: CozyColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCozyColors.current
}

@Composable
fun CozyTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkCozyColors else LightCozyColors
    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = colors.indigo,
            onPrimary = colors.onIndigo,
            primaryContainer = colors.indigoSoft,
            onPrimaryContainer = colors.indigo,
            secondaryContainer = colors.indigoSoft,
            background = colors.paperBackground,
            onBackground = colors.inkPrimary,
            surface = colors.paperElevated,
            onSurface = colors.inkPrimary,
            onSurfaceVariant = colors.inkSecondary,
            surfaceContainer = colors.paperElevated,
            outline = colors.borderSubtle,
            outlineVariant = colors.borderSubtle,
            error = colors.danger,
        )
    } else {
        lightColorScheme(
            primary = colors.indigo,
            onPrimary = colors.onIndigo,
            primaryContainer = colors.indigoSoft,
            onPrimaryContainer = colors.indigo,
            secondaryContainer = colors.indigoSoft,
            background = colors.paperBackground,
            onBackground = colors.inkPrimary,
            surface = colors.paperElevated,
            onSurface = colors.inkPrimary,
            onSurfaceVariant = colors.inkSecondary,
            surfaceContainer = colors.paperElevated,
            outline = colors.borderSubtle,
            outlineVariant = colors.borderSubtle,
            error = colors.danger,
        )
    }

    CompositionLocalProvider(LocalCozyColors provides colors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = Typography(),
            content = content,
        )
    }
}
