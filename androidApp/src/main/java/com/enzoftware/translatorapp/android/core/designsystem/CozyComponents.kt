package com.enzoftware.translatorapp.android.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.enzoftware.translatorapp.android.R

/** Pill chip that copies [text] and flips to a sage "Copied" state for 1.5s. */
@Composable
fun CozyCopyChip(
    text: String,
    modifier: Modifier = Modifier,
    onCopy: ((String) -> Unit)? = null,
) {
    val colors = CozyTheme.colors
    val clipboard = LocalClipboard.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var copyCount by remember { mutableIntStateOf(0) }
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copyCount) {
        if (copyCount == 0) return@LaunchedEffect
        copied = true
        delay(1_500)
        copied = false
    }

    val content by animateColorAsState(
        if (copied) colors.sage else colors.inkSecondary,
        CozyMotion.snappy(),
        label = "copyContent",
    )
    val container by animateColorAsState(
        if (copied) colors.sage.copy(alpha = 0.14f) else colors.paperMuted.copy(alpha = 0.85f),
        CozyMotion.snappy(),
        label = "copyContainer",
    )
    val description = stringResource(if (copied) R.string.cd_copied else R.string.cd_copy_text)

    Row(
        modifier = modifier
            .cozyClickable(pressedScale = 0.94f) {
                if (onCopy != null) {
                    onCopy(text)
                } else {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(android.content.ClipData.newPlainText("Parley", text)),
                        )
                    }
                }
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                copyCount++
            }
            .semantics(mergeDescendants = true) { contentDescription = description }
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        AnimatedContent(
            targetState = copied,
            transitionSpec = { (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith fadeOut() },
            label = "copyIcon",
        ) { isCopied ->
            Icon(
                imageVector = if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(13.dp),
            )
        }
        Text(
            text = stringResource(if (copied) R.string.copied else R.string.copy),
            style = CozyText.sans(CozyTextSize.caption, FontWeight.SemiBold),
            color = content,
        )
    }
}

/** "Listen" / "Speaking" capsule that toggles text-to-speech. */
@Composable
fun CozySpeakButton(
    isSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CozyTheme.colors
    val haptics = LocalHapticFeedback.current
    val content by animateColorAsState(
        if (isSpeaking) colors.onIndigo else colors.indigo,
        CozyMotion.snappy(),
        label = "speakContent",
    )
    val container by animateColorAsState(
        if (isSpeaking) colors.indigo else colors.indigoSoft,
        CozyMotion.snappy(),
        label = "speakContainer",
    )
    val description = stringResource(if (isSpeaking) R.string.cd_stop_speaking else R.string.cd_speak_translation)

    Row(
        modifier = modifier
            .cozyClickable(pressedScale = 0.94f) {
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onClick()
            }
            .semantics(mergeDescendants = true) { contentDescription = description }
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = if (isSpeaking) Icons.Rounded.GraphicEq else Icons.AutoMirrored.Rounded.VolumeUp,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(if (isSpeaking) R.string.speaking else R.string.listen),
            style = CozyText.sans(CozyTextSize.caption, FontWeight.SemiBold),
            color = content,
        )
    }
}

/** Floating error banner shown at the top of the translate screen. */
@Composable
fun CozyBannerToast(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CozyTheme.colors
    Row(
        modifier = modifier
            .cozyFloatingChrome(RoundedCornerShape(CozyRadius.inner), tint = colors.paperElevated)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = null,
            tint = colors.danger,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = message,
            style = CozyText.sans(CozyTextSize.subheadline, FontWeight.Medium),
            color = colors.inkPrimary,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        CozyIconCircle(
            icon = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.cd_dismiss_error),
            size = 26,
            iconSize = 12,
            onClick = onDismiss,
        )
    }
}

/** Small muted circular icon button (dismiss / close). */
@Composable
fun CozyIconCircle(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 30,
    iconSize: Int = 14,
) {
    val colors = CozyTheme.colors
    Box(
        modifier = modifier
            .cozyClickable(onClick = onClick)
            .size(size.dp)
            .clip(CircleShape)
            .background(colors.paperMuted.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.inkSecondary,
            modifier = Modifier.size(iconSize.dp),
        )
    }
}

/** Round floating toolbar button (settings gear, back chevron, voice close). */
@Composable
fun CozyChromeIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 40,
) {
    val colors = CozyTheme.colors
    Box(
        modifier = modifier
            .cozyClickable(onClick = onClick)
            .size(size.dp)
            .cozyFloatingChrome(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.inkPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}
