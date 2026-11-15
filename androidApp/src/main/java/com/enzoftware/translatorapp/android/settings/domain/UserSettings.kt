package com.enzoftware.translatorapp.android.settings.domain

import kotlinx.coroutines.flow.Flow

enum class AppTheme {
    LIGHT,
    DARK;

    val isDark: Boolean get() = this == DARK

    companion object {
        val Default = LIGHT

        fun fromStorage(value: String?): AppTheme =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Default
    }
}

data class UserSettings(
    val userName: String = "",
    val theme: AppTheme = AppTheme.Default,
) {
    val trimmedUserName: String get() = userName.trim()

    /** Up to two initials from the name, or "P" (Parley) when no name is set — same as iOS. */
    val initials: String
        get() = trimmedUserName
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first() }
            .joinToString("")
            .uppercase()
            .ifEmpty { "P" }
}

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setUserName(name: String)
    suspend fun setTheme(theme: AppTheme)
}

data class AppVersionInfo(
    val appName: String,
    val shortVersion: String,
    val buildNumber: String,
) {
    val displayVersion: String
        get() = if (buildNumber.isEmpty() || buildNumber == shortVersion) {
            shortVersion
        } else {
            "$shortVersion ($buildNumber)"
        }
}
