package com.enzoftware.translatorapp.android.translate.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enzoftware.translatorapp.android.translate.presentation.components.LanguageDropDown
import com.enzoftware.translatorapp.android.translate.presentation.components.SwapLanguageButton
import com.enzoftware.translatorapp.translate.presentation.TranslateEvent
import com.enzoftware.translatorapp.translate.presentation.TranslateState

@Composable
fun TranslateScreen(
    state: TranslateState,
    onEvent: (TranslateEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        floatingActionButton = {

        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LanguageDropDown(
                        uiLanguage = state.fromLanguage,
                        isOpen = state.isChoosingFromLanguage,
                        onClick = {
                            onEvent(TranslateEvent.OpenFromLanguageDropDown)
                        },
                        onDismiss = {
                            onEvent(TranslateEvent.StopChoosingLanguage)
                        },
                        onLanguageSelected = { language ->
                            onEvent(TranslateEvent.ChooseFromLanguage(language))
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    SwapLanguageButton {
                        onEvent(TranslateEvent.SwapLanguages)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    LanguageDropDown(
                        uiLanguage = state.toLanguage,
                        isOpen = state.isChoosingToLanguage,
                        onClick = {
                            onEvent(TranslateEvent.OpenToLanguageDropDown)
                        },
                        onDismiss = {
                            onEvent(TranslateEvent.StopChoosingLanguage)
                        },
                        onLanguageSelected = { language ->
                            onEvent(TranslateEvent.ChooseToLanguage(language))
                        }
                    )
                }
            }
        }
    }
}