package com.enzoftware.translatorapp.android.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.enzoftware.translatorapp.android.settings.data.DataStoreSettingsRepository
import com.enzoftware.translatorapp.android.settings.domain.AppTheme
import com.enzoftware.translatorapp.android.settings.domain.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreSettingsRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun repository(fileName: String = "settings.preferences_pb") = DataStoreSettingsRepository(
        PreferenceDataStoreFactory.create(scope = scope) { tmp.root.resolve(fileName) },
    )

    @Test
    fun `defaults to an empty name and the light theme`() = runTest {
        assertThat(repository().settings.first()).isEqualTo(UserSettings(userName = "", theme = AppTheme.LIGHT))
    }

    @Test
    fun `persists the name and theme`() = runTest {
        val repository = repository()

        repository.settings.test {
            assertThat(awaitItem()).isEqualTo(UserSettings())

            repository.setUserName("Ada Lovelace")
            assertThat(awaitItem()).isEqualTo(UserSettings(userName = "Ada Lovelace"))

            repository.setTheme(AppTheme.DARK)
            assertThat(awaitItem()).isEqualTo(UserSettings(userName = "Ada Lovelace", theme = AppTheme.DARK))
        }
    }
}
