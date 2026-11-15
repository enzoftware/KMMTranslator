package com.enzoftware.translatorapp.android.translate.presentation

import androidx.annotation.StringRes
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.translate.domain.translate.TranslateError
import java.util.Locale

/** Time-of-day greeting shown above the name, using the same hour buckets as iOS. */
@StringRes
fun timeGreetingRes(hourOfDay: Int): Int = when (hourOfDay) {
    in 5..11 -> R.string.greeting_morning
    in 12..16 -> R.string.greeting_afternoon
    in 17..21 -> R.string.greeting_evening
    else -> R.string.greeting_late_night
}

@StringRes
fun TranslateError.messageRes(): Int = when (this) {
    TranslateError.SERVICE_UNAVAILABLE -> R.string.error_service_unavailable
    TranslateError.CLIENT_ERROR -> R.string.error_client
    TranslateError.SERVER_ERROR -> R.string.error_server
    TranslateError.UNKNOWN_ERROR -> R.string.error_unknown
}

/** Locale for text-to-speech; falls back to the ISO code for languages without a JDK constant. */
fun UiLanguage.speechLocale(): Locale = toLocale() ?: Locale.forLanguageTag(language.code)
