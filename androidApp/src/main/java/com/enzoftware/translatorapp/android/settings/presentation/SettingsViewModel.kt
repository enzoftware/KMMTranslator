package com.enzoftware.translatorapp.android.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enzoftware.translatorapp.android.settings.domain.AppTheme
import com.enzoftware.translatorapp.android.settings.domain.SettingsRepository
import com.enzoftware.translatorapp.android.settings.domain.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {

    /**
     * Name edits are applied immediately so the text field never waits on a DataStore round-trip
     * (which would make the cursor jump), and are persisted in the background.
     */
    private val nameDraft = MutableStateFlow<String?>(null)

    val settings: StateFlow<UserSettings> = combine(repository.settings, nameDraft) { stored, draft ->
        if (draft == null) stored else stored.copy(userName = draft)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    fun onUserNameChange(name: String) {
        nameDraft.value = name
        viewModelScope.launch { repository.setUserName(name) }
    }

    fun onThemeChange(theme: AppTheme) {
        viewModelScope.launch { repository.setTheme(theme) }
    }
}
