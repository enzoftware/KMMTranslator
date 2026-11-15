package com.enzoftware.translatorapp.android.translate

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.enzoftware.translatorapp.android.translate.presentation.components.EditorTextSync
import org.junit.Test

class EditorTextSyncTest {

    private fun EditorTextSync.type(text: String) = onUserEdit(TextFieldValue(text, TextRange(text.length)))

    @Test
    fun `late echoes of earlier keystrokes never overwrite newer text`() {
        val sync = EditorTextSync("")
        listOf("B", "Br", "Bre", "Brea", "Break").forEach { sync.type(it) }

        // The ViewModel state catches up one emission at a time, well behind the user.
        listOf("B", "Br", "Bre").forEach { sync.onStateText(it) }
        assertThat(sync.value.text).isEqualTo("Break")

        listOf("Brea", "Break").forEach { sync.onStateText(it) }
        assertThat(sync.value.text).isEqualTo("Break")
        assertThat(sync.value.selection).isEqualTo(TextRange(5))
    }

    @Test
    fun `conflated echoes are also recognised`() {
        val sync = EditorTextSync("")
        listOf("H", "He", "Hel", "Hell", "Hello").forEach { sync.type(it) }

        sync.onStateText("Hel") // StateFlow may skip intermediate values
        sync.onStateText("Hello")

        assertThat(sync.value.text).isEqualTo("Hello")
    }

    @Test
    fun `external changes such as a voice result replace the text`() {
        val sync = EditorTextSync("Hello")

        sync.onStateText("good morning")

        assertThat(sync.value.text).isEqualTo("good morning")
        assertThat(sync.value.selection).isEqualTo(TextRange("good morning".length))
    }

    @Test
    fun `clearing locally is not undone by stale echoes`() {
        val sync = EditorTextSync("")
        sync.type("Hi")
        sync.replace("")

        sync.onStateText("Hi") // stale echo from before the clear
        assertThat(sync.value.text).isEqualTo("")

        sync.onStateText("") // the clear lands
        assertThat(sync.value.text).isEqualTo("")
    }

    @Test
    fun `only text changes are reported to the view model`() {
        val sync = EditorTextSync("Hello")

        assertThat(sync.onUserEdit(TextFieldValue("Hello", TextRange(0)))).isFalse() // cursor move only
        assertThat(sync.type("Hello!")).isTrue()
    }
}
