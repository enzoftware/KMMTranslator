package com.enzoftware.translatorapp.android.translate.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.enzoftware.translatorapp.core.presentation.UiLanguage

@Composable
fun LanguageDropDownItem(
    uiLanguage: UiLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        onClick = onClick,
        modifier = modifier,
        text = { Text(text = uiLanguage.language.name.uppercase()) },
        leadingIcon = {
            Image(
                painterResource(id = uiLanguage.drawableRes),
                contentDescription = uiLanguage.language.name,
                modifier = Modifier.size(24.dp)
            )
        }
    )
}
