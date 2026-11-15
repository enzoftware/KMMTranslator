package com.enzoftware.translatorapp.phrasebook

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThanOrEqualTo
import com.enzoftware.translatorapp.core.domain.language.Language
import com.enzoftware.translatorapp.phrasebook.domain.Phrasebook
import com.enzoftware.translatorapp.phrasebook.domain.PhrasebookCategory
import kotlin.test.Test

class PhrasebookTest {

    @Test
    fun everyEntryHasANonBlankTranslationForEverySupportedLanguage() {
        val gaps = Phrasebook.entries.flatMap { entry ->
            Language.entries.filter { entry.translations[it].isNullOrBlank() }.map { "${entry.key}/${it.name}" }
        }
        assertThat(gaps).isEmpty()
    }

    @Test
    fun keysAreUniqueAndEveryCategoryIsUsed() {
        assertThat(Phrasebook.entries.map { it.key }.toSet().size).isEqualTo(Phrasebook.entries.size)
        assertThat(Phrasebook.entries.map { it.category }.toSet()).isEqualTo(PhrasebookCategory.entries.toSet())
        assertThat(Phrasebook.entries.size).isGreaterThanOrEqualTo(24)
    }

    @Test
    fun entriesCanBeLookedUpByKey() {
        assertThat(Phrasebook.byKey("thank_you")?.textIn(Language.GERMAN)).isEqualTo("Danke")
        assertThat(Phrasebook.byKey("missing")).isEqualTo(null)
    }
}
