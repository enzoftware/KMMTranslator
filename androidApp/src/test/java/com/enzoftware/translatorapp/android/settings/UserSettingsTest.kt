package com.enzoftware.translatorapp.android.settings

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.enzoftware.translatorapp.android.settings.domain.AppTheme
import com.enzoftware.translatorapp.android.settings.domain.AppVersionInfo
import com.enzoftware.translatorapp.android.settings.domain.UserSettings
import org.junit.Test

class UserSettingsTest {

    @Test
    fun `initials default to P when the name is blank`() {
        assertThat(UserSettings(userName = "").initials).isEqualTo("P")
        assertThat(UserSettings(userName = "   ").initials).isEqualTo("P")
    }

    @Test
    fun `initials use the first letter of up to two words, uppercased`() {
        assertThat(UserSettings(userName = "ada").initials).isEqualTo("A")
        assertThat(UserSettings(userName = "ada lovelace").initials).isEqualTo("AL")
        assertThat(UserSettings(userName = "  grace   brewster murray hopper ").initials).isEqualTo("GB")
    }

    @Test
    fun `trimmed user name drops surrounding whitespace only`() {
        assertThat(UserSettings(userName = "  Ada Lovelace \n").trimmedUserName).isEqualTo("Ada Lovelace")
    }

    @Test
    fun `theme defaults to light like iOS`() {
        assertThat(UserSettings().theme).isEqualTo(AppTheme.LIGHT)
        assertThat(AppTheme.LIGHT.isDark).isFalse()
        assertThat(AppTheme.DARK.isDark).isTrue()
    }

    @Test
    fun `theme is restored from storage and falls back to the default`() {
        assertThat(AppTheme.fromStorage("dark")).isEqualTo(AppTheme.DARK)
        assertThat(AppTheme.fromStorage("LIGHT")).isEqualTo(AppTheme.LIGHT)
        assertThat(AppTheme.fromStorage(null)).isEqualTo(AppTheme.LIGHT)
        assertThat(AppTheme.fromStorage("sepia")).isEqualTo(AppTheme.LIGHT)
    }

    @Test
    fun `display version hides the build number when it adds nothing`() {
        assertThat(AppVersionInfo("Parley", "1.0", "1.0").displayVersion).isEqualTo("1.0")
        assertThat(AppVersionInfo("Parley", "1.0", "").displayVersion).isEqualTo("1.0")
        assertThat(AppVersionInfo("Parley", "1.0", "42").displayVersion).isEqualTo("1.0 (42)")
    }
}
