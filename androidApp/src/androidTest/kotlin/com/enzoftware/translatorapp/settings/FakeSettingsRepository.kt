package com.enzoftware.translatorapp.settings

import com.enzoftware.translatorapp.android.settings.domain.AppTheme
import com.enzoftware.translatorapp.android.settings.domain.SettingsRepository
import com.enzoftware.translatorapp.android.settings.domain.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory settings so instrumented tests never leak a name or theme into each other. */
class FakeSettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(UserSettings())

    override suspend fun setUserName(name: String) {
        settings.update { it.copy(userName = name) }
    }

    override suspend fun setTheme(theme: AppTheme) {
        settings.update { it.copy(theme = theme) }
    }
}
