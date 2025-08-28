package com.enzoftware.translatorapp.android.translate.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.translate.presentation.components.LanguageDropDown
import com.enzoftware.translatorapp.android.translate.presentation.components.SwapLanguageButton
import com.enzoftware.translatorapp.android.translate.presentation.components.TranslateTextField
import com.enzoftware.translatorapp.translate.presentation.TranslateEvent
import com.enzoftware.translatorapp.translate.presentation.TranslateState

@Composable
fun TranslateScreen(
    state: TranslateState,
    onEvent: (TranslateEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
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

            item {
                val clipboardManager = LocalClipboardManager.current
                val keyboardController = LocalSoftwareKeyboardController.current

                TranslateTextField(
                    fromText = state.fromText,
                    toText = state.toText,
                    isTranslating = state.isTranslating,
                    fromLanguage = state.fromLanguage,
                    toLanguage = state.toLanguage,
                    onTranslateClick = {
                        onEvent(TranslateEvent.Translate)
                    },
                    onTextChange = { text ->
                        onEvent(TranslateEvent.ChangeTranslationText(text))
                    },
                    onCopyClick = { text ->
                        keyboardController?.hide()
                        clipboardManager.setText(buildAnnotatedString { append(text) })
                        Toast.makeText(context, context.getString(R.string.translation_copied), Toast.LENGTH_SHORT)
                            .show()
                    },
                    onCloseClick = {
                        onEvent(TranslateEvent.CloseTranslation)
                    },
                    onSpeakerClick = {

                    },
                    onTextFieldClick = {
                        onEvent(TranslateEvent.EditTranslation)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}