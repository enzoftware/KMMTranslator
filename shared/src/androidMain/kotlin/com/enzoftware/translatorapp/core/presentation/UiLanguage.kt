package com.enzoftware.translatorapp.core.presentation

import androidx.annotation.DrawableRes
import com.enzoftware.translatorapp.core.domain.language.Language
import com.enzoftware.translatorapp.R
import java.util.Locale

actual class UiLanguage(
    @DrawableRes val drawableRes: Int,
    actual val language: Language
) {
    fun toLocale(): Locale? {
        return when (language) {
            Language.ENGLISH -> Locale.ENGLISH
            Language.FRENCH -> Locale.FRENCH
            Language.CHINESE -> Locale.CHINESE
            Language.GERMAN -> Locale.GERMAN
            Language.ITALIAN -> Locale.ITALIAN
            Language.JAPANESE -> Locale.JAPAN
            Language.KOREAN -> Locale.KOREAN
            else -> null
        }
    }
    actual companion object {
        actual fun byCode(code: String): UiLanguage {
            return allLanguages.find { it.language.code == code } ?: throw IllegalArgumentException("No language with code $code")
        }
        actual val allLanguages: List<UiLanguage>
            get() = Language.entries.map { language ->
                UiLanguage(
                    language = language,
                    drawableRes = when(language) {
                        Language.ENGLISH -> R.drawable.english
                        Language.ARABIC -> R.drawable.arabic
                        Language.AZERBAIJANI -> R.drawable.azerbaijani
                        Language.CHINESE -> R.drawable.chinese
                        Language.FRENCH -> R.drawable.french
                        Language.GERMAN -> R.drawable.german
                        Language.ITALIAN -> R.drawable.italian
                        Language.JAPANESE -> R.drawable.japanese
                        Language.KOREAN -> R.drawable.korean
                        Language.SPANISH -> R.drawable.spanish
                        else -> -1
                    }
                )
            }.sortedBy { it.language.name }

    }


}