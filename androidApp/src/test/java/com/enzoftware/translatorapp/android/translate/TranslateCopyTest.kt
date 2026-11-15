package com.enzoftware.translatorapp.android.translate

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.translate.presentation.messageRes
import com.enzoftware.translatorapp.android.translate.presentation.speechLocale
import com.enzoftware.translatorapp.android.translate.presentation.timeGreetingRes
import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.translate.domain.translate.TranslateError
import org.junit.Test
import java.util.Locale

class TranslateCopyTest {

    @Test
    fun `greeting buckets match iOS`() {
        val expected = mapOf(
            0 to R.string.greeting_late_night,
            4 to R.string.greeting_late_night,
            5 to R.string.greeting_morning,
            11 to R.string.greeting_morning,
            12 to R.string.greeting_afternoon,
            16 to R.string.greeting_afternoon,
            17 to R.string.greeting_evening,
            21 to R.string.greeting_evening,
            22 to R.string.greeting_late_night,
            23 to R.string.greeting_late_night,
        )
        expected.forEach { (hour, res) ->
            assertThat(timeGreetingRes(hour), "hour $hour").isEqualTo(res)
        }
    }

    @Test
    fun `every translate error has its own message`() {
        assertThat(TranslateError.SERVICE_UNAVAILABLE.messageRes()).isEqualTo(R.string.error_service_unavailable)
        assertThat(TranslateError.CLIENT_ERROR.messageRes()).isEqualTo(R.string.error_client)
        assertThat(TranslateError.SERVER_ERROR.messageRes()).isEqualTo(R.string.error_server)
        assertThat(TranslateError.UNKNOWN_ERROR.messageRes()).isEqualTo(R.string.error_unknown)
    }

    @Test
    fun `speech locale falls back to the language code`() {
        assertThat(UiLanguage.byCode("en").speechLocale()).isEqualTo(Locale.ENGLISH)
        // No JDK constant for Spanish or Arabic: use the ISO code instead of silently speaking English.
        assertThat(UiLanguage.byCode("es").speechLocale().language).isEqualTo("es")
        assertThat(UiLanguage.byCode("ar").speechLocale().language).isEqualTo("ar")
    }
}
