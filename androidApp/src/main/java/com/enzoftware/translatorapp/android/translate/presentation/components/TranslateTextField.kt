package com.enzoftware.translatorapp.android.translate.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.theme.LightBlue
import com.enzoftware.translatorapp.core.presentation.UiLanguage

@Composable
fun TranslateTextField(
    modifier: Modifier = Modifier,
    fromText: String,
    toText: String?,
    isTranslating: Boolean,
    fromLanguage: UiLanguage,
    toLanguage: UiLanguage,
    onTranslateClick: () -> Unit,
    onTextChange: (String) -> Unit,
    onCopyClick: (String) -> Unit,
    onCloseClick: () -> Unit,
    onSpeakerClick: () -> Unit,
    onTextFieldClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .gradientSurface()
            .clickable(onClick = onTextFieldClick)
            .padding(16.dp)
    ) {
        AnimatedContent(targetState = toText) { toText ->
            if (toText == null || isTranslating) {
                IdleTranslateTextField(
                    fromText = fromText,
                    isTranslating = isTranslating,
                    onTextChange = onTextChange,
                    onTranslateClick = onTranslateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f),
                )
            } else {
                TranslatedTextField(
                    fromText = fromText,
                    toText = toText,
                    fromLanguage = fromLanguage,
                    toLanguage = toLanguage,
                    onCopyClick = onCopyClick,
                    onCloseClick = onCloseClick,
                    onSpeakerClick = onSpeakerClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TranslatedTextField(
    fromText: String,
    toText: String,
    fromLanguage: UiLanguage,
    toLanguage: UiLanguage,
    onCopyClick: (String) -> Unit,
    onCloseClick: () -> Unit,
    onSpeakerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        LanguageDisplay(language = fromLanguage)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = fromText, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            IconButton(
                onClick = {
                    onCopyClick(fromText)
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.copy),
                    contentDescription = stringResource(id = R.string.copy),
                    tint = LightBlue
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onCloseClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(id = R.string.close),
                    tint = LightBlue
                )
            }

        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        LanguageDisplay(language = toLanguage)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = toText, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            IconButton(
                onClick = {
                    onCopyClick(toText)
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.copy),
                    contentDescription = stringResource(id = R.string.copy),
                    tint = LightBlue
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSpeakerClick
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.speaker),
                    contentDescription = stringResource(id = R.string.speaker),
                    tint = LightBlue
                )
            }

        }
    }
}

@Composable
private fun IdleTranslateTextField(
    fromText: String,
    isTranslating: Boolean,
    onTextChange: (String) -> Unit,
    onTranslateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        BasicTextField(
            value = fromText,
            onValueChange = { onTextChange(it) },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
            )
        )

        if (fromText.isEmpty() && !isTranslating) {
            Text(text = stringResource(id = R.string.enter_text), color = LightBlue)
        }

        ProgressButton(
            text = stringResource(R.string.translate),
            isLoading = isTranslating,
            onClick = onTranslateClick,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }

}