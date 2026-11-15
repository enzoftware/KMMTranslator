package com.enzoftware.translatorapp.android.translate.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozySpacing
import com.enzoftware.translatorapp.android.core.designsystem.CozyText
import com.enzoftware.translatorapp.android.core.designsystem.CozyTextSize
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.core.designsystem.cozyCard
import com.enzoftware.translatorapp.android.core.designsystem.cozyClickable
import com.enzoftware.translatorapp.translate.presentation.UiHistoryItem

/** A saved translation, rendered as a fixed-size postcard on the shelf or a full-width row. */
@Composable
fun TranslateHistoryItem(
    item: UiHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPostcardShelf: Boolean = false,
) {
    val colors = CozyTheme.colors
    val sizeModifier = if (isPostcardShelf) {
        Modifier
            .width(252.dp + CozySpacing.md * 2)
            .heightIn(min = 132.dp + CozySpacing.md * 2)
    } else {
        Modifier.fillMaxWidth()
    }

    Column(
        modifier = modifier
            .cozyClickable(onClick = onClick)
            .then(sizeModifier)
            .cozyCard(cornerRadius = 22.dp)
            .padding(CozySpacing.md),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.paperMuted.copy(alpha = 0.65f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                SmallLanguageIcon(language = item.fromLanguage, size = 18.dp)
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = colors.inkMuted,
                    modifier = Modifier.size(10.dp),
                )
                SmallLanguageIcon(language = item.toLanguage, size = 18.dp)
            }
            Text(
                text = stringResource(
                    R.string.language_pair,
                    item.fromLanguage.language.languageName,
                    item.toLanguage.language.languageName,
                ),
                style = CozyText.sans(CozyTextSize.caption2, FontWeight.SemiBold),
                color = colors.inkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colors.indigoSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.NorthWest, contentDescription = null, tint = colors.indigo, modifier = Modifier.size(12.dp))
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = item.toText,
                style = CozyText.serif(CozyTextSize.headline, FontWeight.SemiBold),
                color = colors.inkPrimary,
                maxLines = if (isPostcardShelf) 2 else 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.fromText,
                style = CozyText.sans(CozyTextSize.caption),
                color = colors.inkSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isPostcardShelf) Spacer(Modifier.weight(1f, fill = false))
    }
}
