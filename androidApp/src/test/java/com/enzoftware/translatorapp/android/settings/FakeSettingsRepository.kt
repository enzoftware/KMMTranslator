package com.enzoftware.translatorapp.android.settings

import com.enzoftware.translatorapp.android.settings.domain.AppTheme
import com.enzoftware.translatorapp.android.settings.domain.SettingsRepository
import com.enzoftware.translatorapp.android.settings.domain.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(initial: UserSettings = UserSettings()) : SettingsRepository {
    override val settings = MutableStateFlow(initial)
    val savedNames = mutableListOf<String>()

    override suspend fun setUserName(name: String) {
        savedNames += name
        settings.update { it.copy(userName = name) }
    }

    override suspend fun setTheme(theme: AppTheme) {
        settings.update { it.copy(theme = theme) }
    }
}
