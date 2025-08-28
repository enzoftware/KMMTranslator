package com.enzoftware.translatorapp.android.translate.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ProgressButton(
    modifier: Modifier = Modifier,
    text: String,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(targetState = isLoading) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    strokeWidth = 4.dp
                )
            } else {
                Text(
                    text = text.uppercase(),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

        }
    }
}