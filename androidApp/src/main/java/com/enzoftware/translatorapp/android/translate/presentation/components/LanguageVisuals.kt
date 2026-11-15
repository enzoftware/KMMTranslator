package com.enzoftware.translatorapp.android.translate.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enzoftware.translatorapp.android.core.designsystem.CozyText
import com.enzoftware.translatorapp.android.core.designsystem.CozyTextSize
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.core.presentation.UiLanguage

/** Circular flag with a subtle border; falls back to the language code on an indigo badge. */
@Composable
fun SmallLanguageIcon(
    language: UiLanguage,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
) {
    val colors = CozyTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.dp, colors.borderSubtle, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (language.drawableRes > 0) {
            Image(
                painter = painterResource(language.drawableRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(colors.indigoSoft),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = language.language.code.uppercase(),
                    style = CozyText.rounded(maxOf(9f, size.value * 0.36f).sp),
                    color = colors.indigo,
                )
            }
        }
    }
}

/** Flag + language name on a muted capsule. */
@Composable
fun LanguageDisplay(
    language: UiLanguage,
    modifier: Modifier = Modifier,
) {
    val colors = CozyTheme.colors
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.paperMuted.copy(alpha = 0.65f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SmallLanguageIcon(language = language, size = 22.dp)
        Text(
            text = language.language.languageName,
            style = CozyText.sans(CozyTextSize.subheadline, FontWeight.SemiBold),
            color = colors.inkSecondary,
        )
    }
}
