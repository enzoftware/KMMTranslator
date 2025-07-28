package com.enzoftware.translatorapp.core.domain.language

enum class Language(
    val code: String,
    val languageName: String,
) {
    ENGLISH("en", "English"),
    ARABIC("ar", "Arabic"),
    AZERBAIJANI("az", "Azerbaijani"),
    CHINESE("zh", "Chinese"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    ITALIAN("it", "Italian"),
    JAPANESE("ja", "Japanese"),
    KOREAN("ko", "Korean"),
    SPANISH("es", "Spanish");

    companion object {
        fun byCode(code: String): Language {
            return entries.find { it.code == code }
                ?: throw IllegalArgumentException("Invalid or unsupported language code")
        }
    }

}