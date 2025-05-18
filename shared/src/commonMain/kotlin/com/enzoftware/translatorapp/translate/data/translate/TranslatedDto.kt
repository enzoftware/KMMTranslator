package com.enzoftware.translatorapp.translate.data.translate

import kotlinx.serialization.Serializable

@Serializable
data class TranslatedDto(
    val translatedText: String
)