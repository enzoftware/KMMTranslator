package com.enzoftware.translatorapp.android.settings

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.enzoftware.translatorapp.android.settings.domain.AppTheme
import com.enzoftware.translatorapp.android.settings.domain.UserSettings
import com.enzoftware.translatorapp.android.settings.presentation.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var repository: FakeSettingsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeSettingsRepository(UserSettings(userName = "Ada", theme = AppTheme.LIGHT))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `exposes the stored settings`() = runTest {
        val viewModel = SettingsViewModel(repository)

        assertThat(viewModel.settings.value).isEqualTo(UserSettings(userName = "Ada", theme = AppTheme.LIGHT))
    }

    @Test
    fun `is not loaded until the repository emits`() = runTest {
        val gate = kotlinx.coroutines.flow.MutableSharedFlow<UserSettings>()
        val slowRepository = object : com.enzoftware.translatorapp.android.settings.domain.SettingsRepository by repository {
            override val settings = gate
        }
        val viewModel = SettingsViewModel(slowRepository)
        assertThat(viewModel.isLoaded.value).isEqualTo(false)

        gate.emit(UserSettings(theme = AppTheme.DARK))

        assertThat(viewModel.isLoaded.value).isEqualTo(true)
        assertThat(viewModel.settings.value.theme).isEqualTo(AppTheme.DARK)
    }

    @Test
    fun `name edits show up immediately and are persisted`() = runTest {
        val viewModel = SettingsViewModel(repository)

        viewModel.onUserNameChange("Ada L")

        assertThat(viewModel.settings.value.userName).isEqualTo("Ada L")
        assertThat(repository.savedNames).containsExactly("Ada L")
        assertThat(repository.settings.value.userName).isEqualTo("Ada L")
    }

    @Test
    fun `clearing the name falls back to the default initials`() = runTest {
        val viewModel = SettingsViewModel(repository)

        viewModel.onUserNameChange("")

        assertThat(viewModel.settings.value.initials).isEqualTo("P")
    }

    @Test
    fun `theme changes are persisted and exposed`() = runTest {
        val viewModel = SettingsViewModel(repository)

        viewModel.onThemeChange(AppTheme.DARK)

        assertThat(repository.settings.value.theme).isEqualTo(AppTheme.DARK)
        assertThat(viewModel.settings.value.theme).isEqualTo(AppTheme.DARK)
    }
}
