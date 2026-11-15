package com.enzoftware.translatorapp.translate.presentation

import com.enzoftware.translatorapp.core.presentation.UiLanguage

sealed class TranslateEvent {
    data class ChooseFromLanguage(val language: UiLanguage) : TranslateEvent()
    data class ChooseToLanguage(val language: UiLanguage) : TranslateEvent()
    object StopChoosingLanguage : TranslateEvent()
    object SwapLanguages : TranslateEvent()
    data class ChangeTranslationText(val text: String) : TranslateEvent()
    object Translate : TranslateEvent()
    object OpenFromLanguageDropDown : TranslateEvent()
    object OpenToLanguageDropDown : TranslateEvent()
    object CloseTranslation : TranslateEvent()
    data class SelectHistoryItem(val item: UiHistoryItem) : TranslateEvent()
    object EditTranslation : TranslateEvent()
    object RecordAudio : TranslateEvent()
    data class SubmitVoiceResult(val result: String?) : TranslateEvent()
    object OnError : TranslateEvent()

    /**
     * Shows a built-in phrasebook translation, offline and without saving history. With language
     * codes (a saved word) the pair switches to them; otherwise the current pair is used.
     */
    data class ShowPhrasebookEntry(
        val entryKey: String,
        val fromLanguageCode: String? = null,
        val toLanguageCode: String? = null,
    ) : TranslateEvent()
}
