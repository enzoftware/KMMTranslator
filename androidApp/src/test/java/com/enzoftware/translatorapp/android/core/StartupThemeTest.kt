package com.enzoftware.translatorapp.android.core

import android.app.UiModeManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.enzoftware.translatorapp.android.core.presentation.StartupTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class StartupThemeTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val uiModeManager get() = context.getSystemService(UiModeManager::class.java)

    @Test
    fun `defaults to light before any theme is known`() {
        assertThat(StartupTheme(context).isDark).isFalse()
    }

    @Test
    fun `remembers the chosen theme for the next launch and its splash`() {
        StartupTheme(context).remember(isDark = true)

        assertThat(StartupTheme(context).isDark).isTrue()
        assertThat(shadowOf(uiModeManager).applicationNightMode).isEqualTo(UiModeManager.MODE_NIGHT_YES)

        StartupTheme(context).remember(isDark = false)

        assertThat(StartupTheme(context).isDark).isFalse()
        assertThat(shadowOf(uiModeManager).applicationNightMode).isEqualTo(UiModeManager.MODE_NIGHT_NO)
    }
}
