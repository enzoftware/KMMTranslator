package com.enzoftware.translatorapp.android.settings.presentation

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozyAmbientBackground
import com.enzoftware.translatorapp.android.core.designsystem.CozyChromeIconButton
import com.enzoftware.translatorapp.android.core.designsystem.CozyMotion
import com.enzoftware.translatorapp.android.core.designsystem.CozyRadius
import com.enzoftware.translatorapp.android.core.designsystem.CozySpacing
import com.enzoftware.translatorapp.android.core.designsystem.CozyText
import com.enzoftware.translatorapp.android.core.designsystem.CozyTextSize
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.core.designsystem.cozyCard
import com.enzoftware.translatorapp.android.core.designsystem.cozyClickable
import com.enzoftware.translatorapp.android.core.designsystem.cozyEntrance
import com.enzoftware.translatorapp.android.settings.domain.AppTheme
import com.enzoftware.translatorapp.android.settings.domain.AppVersionInfo
import com.enzoftware.translatorapp.android.settings.domain.UserSettings

object SettingsTestTags {
    const val NAME_FIELD = "settings_name_field"
    fun themeOption(theme: AppTheme) = "settings_theme_${theme.name.lowercase()}"
}

@Composable
fun SettingsScreen(
    settings: UserSettings,
    versionInfo: AppVersionInfo,
    onUserNameChange: (String) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        CozyAmbientBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(PaddingValues(start = CozySpacing.md, end = CozySpacing.md, top = CozySpacing.sm, bottom = CozySpacing.xl)),
            verticalArrangement = Arrangement.spacedBy(CozySpacing.lg),
        ) {
            TopBar(onBack = onBack, modifier = Modifier.cozyEntrance(0))
            ProfileSection(settings, onUserNameChange, Modifier.cozyEntrance(50))
            AppearanceSection(settings.theme, onThemeChange, Modifier.cozyEntrance(100))
            AboutSection(versionInfo, Modifier.cozyEntrance(150))
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CozyTheme.colors
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        CozyChromeIconButton(
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.cd_back_to_translator),
            onClick = onBack,
        )
        Spacer(Modifier.size(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.settings),
                style = CozyText.rounded(CozyTextSize.title2),
                color = colors.inkPrimary,
            )
            Text(
                text = stringResource(R.string.settings_subtitle),
                style = CozyText.sans(CozyTextSize.caption),
                color = colors.inkSecondary,
            )
        }
    }
}

@Composable
private fun ProfileSection(
    settings: UserSettings,
    onUserNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CozyTheme.colors
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        if (isFocused) colors.indigo.copy(alpha = 0.6f) else colors.borderSubtle,
        CozyMotion.gentle(),
        label = "nameBorder",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .cozyCard(elevated = true)
            .padding(CozySpacing.md),
        verticalArrangement = Arrangement.spacedBy(CozySpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.indigoSoft),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(targetState = settings.initials, label = "initials") { initials ->
                    Text(initials, style = CozyText.rounded(CozyTextSize.title3), color = colors.indigo)
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = if (settings.trimmedUserName.isEmpty()) {
                        stringResource(R.string.word_traveler)
                    } else {
                        stringResource(R.string.hi_name_exclaim, settings.trimmedUserName)
                    },
                    style = CozyText.rounded(CozyTextSize.headline),
                    color = colors.inkPrimary,
                )
                Text(
                    text = stringResource(R.string.name_hint_caption),
                    style = CozyText.sans(CozyTextSize.caption),
                    color = colors.inkSecondary,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CozyRadius.inner))
                .background(colors.paperMuted.copy(alpha = 0.55f))
                .border(1.dp, borderColor, RoundedCornerShape(CozyRadius.inner))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Person, contentDescription = null, tint = colors.indigo, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (settings.userName.isEmpty()) {
                    Text(
                        text = stringResource(R.string.enter_your_name),
                        style = CozyText.sans(CozyTextSize.body),
                        color = colors.inkMuted,
                    )
                }
                BasicTextField(
                    value = settings.userName,
                    onValueChange = onUserNameChange,
                    singleLine = true,
                    textStyle = CozyText.sans(CozyTextSize.body).copy(color = colors.inkPrimary),
                    cursorBrush = SolidColor(colors.indigo),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SettingsTestTags.NAME_FIELD)
                        .onFocusChanged { isFocused = it.isFocused },
                )
            }
            AnimatedVisibility(
                visible = settings.userName.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Cancel,
                    contentDescription = stringResource(R.string.cd_clear_name),
                    tint = colors.inkMuted,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp)
                        .cozyClickable { onUserNameChange("") },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String) {
    val colors = CozyTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = colors.indigo, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text(title, style = CozyText.rounded(CozyTextSize.headline), color = colors.inkPrimary)
    }
}

@Composable
private fun AppearanceSection(
    selected: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CozyTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .cozyCard(elevated = true)
            .padding(CozySpacing.md),
        verticalArrangement = Arrangement.spacedBy(CozySpacing.md),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            SectionTitle(Icons.Rounded.Palette, stringResource(R.string.appearance))
            Text(
                text = stringResource(R.string.appearance_subtitle),
                style = CozyText.sans(CozyTextSize.caption),
                color = colors.inkSecondary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(CozySpacing.sm)) {
            AppTheme.entries.forEach { theme ->
                ThemeOptionCard(
                    theme = theme,
                    isSelected = theme == selected,
                    onClick = { onThemeChange(theme) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CozyTheme.colors
    val haptics = LocalHapticFeedback.current
    val previewPaper = if (theme.isDark) Color(0xFF171412) else Color(0xFFFAF5EC)
    val previewCard = if (theme.isDark) Color(0xFF2B2622) else Color(0xFFFFFDF8)
    val previewAccent = if (theme.isDark) Color(0xFF7D80F9) else Color(0xFF4B4DED)
    val title = stringResource(if (theme.isDark) R.string.theme_dark else R.string.theme_light)
    val subtitle = stringResource(if (theme.isDark) R.string.theme_dark_subtitle else R.string.theme_light_subtitle)
    val description = stringResource(R.string.cd_theme_option, title)
    val container by animateColorAsState(
        if (isSelected) colors.indigoSoft.copy(alpha = 0.65f) else colors.paperMuted.copy(alpha = 0.4f),
        CozyMotion.cozy(),
        label = "themeContainer",
    )
    val border by animateColorAsState(
        if (isSelected) colors.indigo else colors.borderSubtle,
        CozyMotion.cozy(),
        label = "themeBorder",
    )
    val shape = RoundedCornerShape(CozyRadius.inner)

    Column(
        modifier = modifier
            .cozyClickable(role = Role.RadioButton) {
                if (!isSelected) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onClick()
            }
            .testTag(SettingsTestTags.themeOption(theme))
            .semantics(mergeDescendants = true) {
                contentDescription = description
                selected = isSelected
            }
            .clip(shape)
            .background(container)
            .border(if (isSelected) 1.5.dp else 1.dp, border, shape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(previewPaper)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp)),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(previewCard),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(previewAccent),
                )
            }
            AnimatedContent(
                targetState = isSelected,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.6f)) togetherWith fadeOut() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                label = "themeIcon",
            ) { selectedNow ->
                Icon(
                    imageVector = when {
                        selectedNow -> Icons.Rounded.CheckCircle
                        theme.isDark -> Icons.Rounded.DarkMode
                        else -> Icons.Rounded.LightMode
                    },
                    contentDescription = null,
                    tint = if (selectedNow) colors.indigo else previewAccent,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = CozyText.sans(CozyTextSize.subheadline, FontWeight.Bold), color = colors.inkPrimary)
            Text(subtitle, style = CozyText.sans(CozyTextSize.caption), color = colors.inkSecondary)
        }
    }
}

@Composable
private fun AboutSection(versionInfo: AppVersionInfo, modifier: Modifier = Modifier) {
    val colors = CozyTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .cozyCard(elevated = true)
            .padding(CozySpacing.md),
        verticalArrangement = Arrangement.spacedBy(CozySpacing.sm),
    ) {
        SectionTitle(Icons.Rounded.AutoAwesome, stringResource(R.string.about))
        Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.app_name_label),
                style = CozyText.sans(CozyTextSize.subheadline),
                color = colors.inkSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(
                versionInfo.appName,
                style = CozyText.sans(CozyTextSize.subheadline, FontWeight.SemiBold),
                color = colors.inkPrimary,
            )
        }
        HorizontalDivider(color = colors.borderSubtle)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.version_label),
                style = CozyText.sans(CozyTextSize.subheadline),
                color = colors.inkSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.version_value, versionInfo.displayVersion),
                style = CozyText.sans(CozyTextSize.caption, FontWeight.Bold),
                color = colors.indigo,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.indigoSoft)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

fun Context.appVersionInfo(): AppVersionInfo {
    val info = packageManager.getPackageInfo(packageName, 0)
    return AppVersionInfo(
        appName = applicationInfo.loadLabel(packageManager).toString(),
        shortVersion = info.versionName ?: "1.0",
        buildNumber = PackageInfoCompat.getLongVersionCode(info).toString(),
    )
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    CozyTheme(darkTheme = false) {
        SettingsScreen(
            settings = UserSettings(userName = "Ada Lovelace"),
            versionInfo = AppVersionInfo("Parley", "1.0", "1"),
            onUserNameChange = {},
            onThemeChange = {},
            onBack = {},
        )
    }
}
