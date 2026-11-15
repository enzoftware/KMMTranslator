package com.enzoftware.translatorapp.android.translate.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Keeps the editor text local while the shared `TranslateViewModel` echoes each edit back
 * asynchronously (its state is a `combine(...).stateIn(...)`).
 *
 * Binding the field straight to that state lets a stale echo overwrite newer keystrokes, so fast
 * typing turns "Break a leg" into "Bre leg...ak". Here every user edit is remembered until its echo
 * arrives; echoes are ignored, and only genuinely external changes (starter idioms, voice results,
 * clearing) replace the text.
 */
class EditorTextSync(initialText: String) {

    var value by mutableStateOf(TextFieldValue(initialText, TextRange(initialText.length)))
        private set

    private val pendingEchoes = ArrayDeque<String>()

    /** Applies a user edit. Returns true when the text changed and should be sent to the ViewModel. */
    fun onUserEdit(newValue: TextFieldValue): Boolean {
        val textChanged = newValue.text != value.text
        value = newValue
        if (textChanged) remember(newValue.text)
        return textChanged
    }

    /** Applies a change made by the app itself (idiom chip, clear); its echo is expected too. */
    fun replace(text: String) {
        value = TextFieldValue(text, TextRange(text.length))
        remember(text)
    }

    /** Reconciles with the text currently held by the ViewModel. */
    fun onStateText(text: String) {
        val echoIndex = pendingEchoes.indexOf(text)
        if (echoIndex >= 0) {
            repeat(echoIndex + 1) { pendingEchoes.removeFirst() }
            return
        }
        pendingEchoes.clear()
        if (text != value.text) {
            value = TextFieldValue(text, TextRange(text.length))
        }
    }

    private fun remember(text: String) {
        pendingEchoes.addLast(text)
        while (pendingEchoes.size > MAX_PENDING) pendingEchoes.removeFirst()
    }

    private companion object {
        const val MAX_PENDING = 64
    }
}
