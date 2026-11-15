package com.enzoftware.translatorapp.android.core.presentation

import android.app.UiModeManager
import android.content.Context
import android.os.Build

/**
 * Makes the very first frames (system splash, window background) match the theme picked in
 * Settings, which lives in DataStore and is only known after an async read.
 *
 * - Android 12+: the system draws the splash before the app runs, so the app-level night mode is
 *   set with [UiModeManager.setApplicationNightMode]; `values-night` then supplies the splash colour.
 * - Older versions: a synchronous preference picks the dark starting theme in `onCreate`.
 */
class StartupTheme(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("startup_theme", Context.MODE_PRIVATE)

    val isDark: Boolean get() = prefs.getBoolean(KEY_DARK, false)

    fun remember(isDark: Boolean) {
        if (prefs.getBoolean(KEY_DARK, false) != isDark || !prefs.contains(KEY_DARK)) {
            prefs.edit().putBoolean(KEY_DARK, isDark).apply()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiModeManager = appContext.getSystemService(UiModeManager::class.java)
            // Always set it: `nightMode` can report the system mode, so comparing against it could
            // skip the call exactly when the system and app themes differ. The call is idempotent.
            uiModeManager.setApplicationNightMode(
                if (isDark) UiModeManager.MODE_NIGHT_YES else UiModeManager.MODE_NIGHT_NO,
            )
        }
    }

    private companion object {
        const val KEY_DARK = "dark"
    }
}
