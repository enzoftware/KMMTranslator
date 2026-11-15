package com.enzoftware.translatorapp.android.core.presentation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Translate : Route

    @Serializable
    data class VoiceToText(val languageCode: String) : Route

    @Serializable
    data object Settings : Route
}
