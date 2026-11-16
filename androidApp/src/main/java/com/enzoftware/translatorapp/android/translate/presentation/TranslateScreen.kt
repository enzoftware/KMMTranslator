package com.enzoftware.translatorapp.android.translate.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozyAmbientBackground
import com.enzoftware.translatorapp.android.core.designsystem.CozyBannerToast
import com.enzoftware.translatorapp.android.core.designsystem.CozyChromeIconButton
import com.enzoftware.translatorapp.android.core.designsystem.CozyMotion
import com.enzoftware.translatorapp.android.core.designsystem.CozySpacing
import com.enzoftware.translatorapp.android.core.designsystem.CozyText
import com.enzoftware.translatorapp.android.core.designsystem.CozyTextSize
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.core.designsystem.cozyCard
import com.enzoftware.translatorapp.android.core.designsystem.cozyClickable
import com.enzoftware.translatorapp.android.core.designsystem.cozyEntrance
import com.enzoftware.translatorapp.android.core.designsystem.cozyFloatingChrome
import com.enzoftware.translatorapp.android.settings.domain.UserSettings
import com.enzoftware.translatorapp.android.translate.presentation.components.LanguageDropDown
import com.enzoftware.translatorapp.android.translate.presentation.components.SwapLanguageButton
import com.enzoftware.translatorapp.android.translate.presentation.components.TranslateHistoryItem
import com.enzoftware.translatorapp.android.translate.presentation.components.TranslateTextField
import com.enzoftware.translatorapp.android.translate.presentation.components.rememberTextToSpeechController
import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.translate.presentation.TranslateEvent
import com.enzoftware.translatorapp.translate.presentation.TranslateState
import com.enzoftware.translatorapp.translate.presentation.UiHistoryItem
import java.util.Calendar

object TranslateScreenTestTags {
    const val FROM_LANGUAGE = "from_language_selector"
    const val TO_LANGUAGE = "to_language_selector"
    const val HISTORY_COUNT = "history_count"
    const val POSTCARD_SHELF = "postcard_shelf"
    const val GREETING_TITLE = "greeting_title"
}

@Composable
fun TranslateScreen(
    state: TranslateState,
    settings: UserSettings,
    onEvent: (TranslateEvent) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenPhrasebook: () -> Unit = {},
    hourOfDay: Int = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) },
) {
    val colors = CozyTheme.colors
    val focusManager = LocalFocusManager.current
    val tts = rememberTextToSpeechController()
    var isHistoryExpanded by rememberSaveable { mutableStateOf(false) }

    // Stop speaking whenever the translation or its target language changes (iOS parity).
    LaunchedEffect(state.toText, state.toLanguage.language) { tts.stop() }

    Box(modifier = modifier.fillMaxSize()) {
        CozyAmbientBackground()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            val statusBars = WindowInsets.statusBars.asPaddingValues()
            val navBars = WindowInsets.navigationBars.asPaddingValues()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = statusBars.calculateTopPadding() + CozySpacing.sm,
                    bottom = navBars.calculateBottomPadding() + 108.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(CozySpacing.lg),
            ) {
                item(key = "greeting") {
                    GreetingBar(
                        settings = settings,
                        hourOfDay = hourOfDay,
                        onOpenSettings = onOpenSettings,
                        onOpenPhrasebook = onOpenPhrasebook,
                        modifier = Modifier
                            .padding(horizontal = CozySpacing.md)
                            .cozyEntrance(0),
                    )
                }
                item(key = "translator") {
                    TranslateTextField(
                        fromText = state.fromText,
                        toText = state.toText,
                        isTranslating = state.isTranslating,
                        fromLanguage = state.fromLanguage,
                        toLanguage = state.toLanguage,
                        isSpeaking = tts.isSpeaking,
                        onTranslateEvent = onEvent,
                        onSpeakClick = {
                            state.toText?.let { tts.toggle(it, state.toLanguage.speechLocale()) }
                        },
                        modifier = Modifier
                            .padding(horizontal = CozySpacing.md)
                            .cozyEntrance(60),
                    )
                }
                phrasebookSection(
                    history = state.history,
                    isExpanded = isHistoryExpanded,
                    onToggleExpanded = { isHistoryExpanded = !isHistoryExpanded },
                    onSelect = { item ->
                        tts.stop()
                        onEvent(TranslateEvent.SelectHistoryItem(item))
                    },
                )
            }

            BottomCommandDock(
                state = state,
                onEvent = onEvent,
                onOpenVoice = {
                    focusManager.clearFocus()
                    onEvent(TranslateEvent.RecordAudio)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(CozySpacing.md),
            )
        }

        AnimatedVisibility(
            visible = state.error != null,
            enter = slideInVertically(CozyMotion.cozy()) { -it } + fadeIn(),
            exit = slideOutVertically(CozyMotion.snappy()) { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = CozySpacing.md, vertical = CozySpacing.xs),
        ) {
            // Keep the last message while animating out.
            var lastError by remember { mutableStateOf(state.error) }
            if (state.error != null) lastError = state.error
            lastError?.let { error ->
                CozyBannerToast(
                    message = stringResource(error.messageRes()),
                    onDismiss = { onEvent(TranslateEvent.OnError) },
                )
            }
        }
    }
}

@Composable
private fun GreetingBar(
    settings: UserSettings,
    hourOfDay: Int,
    onOpenSettings: () -> Unit,
    onOpenPhrasebook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CozyTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.indigoSoft)
                .border(1.dp, colors.indigo.copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(targetState = settings.initials, label = "initials") { initials ->
                Text(initials, style = CozyText.rounded(CozyTextSize.subheadline), color = colors.indigo)
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(timeGreetingRes(hourOfDay)).uppercase(),
                style = CozyText.rounded(10.sp).copy(letterSpacing = 1.1.sp),
                color = colors.inkMuted,
            )
            AnimatedContent(
                targetState = settings.trimmedUserName,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "greetingName",
            ) { name ->
                Text(
                    text = if (name.isEmpty()) stringResource(R.string.pocket_phrasebook) else stringResource(R.string.hi_name, name),
                    style = CozyText.serif(CozyTextSize.title3, FontWeight.SemiBold),
                    color = colors.inkPrimary,
                    modifier = Modifier.testTag(TranslateScreenTestTags.GREETING_TITLE),
                )
            }
        }
        CozyChromeIconButton(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            contentDescription = stringResource(R.string.cd_open_phrasebook),
            onClick = onOpenPhrasebook,
        )
        Spacer(Modifier.size(8.dp))
        CozyChromeIconButton(
            icon = Icons.Rounded.Settings,
            contentDescription = stringResource(R.string.cd_open_settings),
            onClick = onOpenSettings,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.phrasebookSection(
    history: List<UiHistoryItem>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelect: (UiHistoryItem) -> Unit,
) {
    item(key = "phrasebook_header") {
        PhrasebookHeader(
            count = history.size,
            isExpanded = isExpanded,
            onToggleExpanded = onToggleExpanded,
            modifier = Modifier
                .padding(horizontal = CozySpacing.md)
                .cozyEntrance(120),
        )
    }
    when {
        history.isEmpty() -> item(key = "phrasebook_empty") {
            EmptyShelf(
                modifier = Modifier
                    .padding(horizontal = CozySpacing.md)
                    .animateItem(),
            )
        }

        isExpanded -> itemsIndexed(history, key = { index, item -> "history_${item.stableKey(index)}" }) { index, item ->
            TranslateHistoryItem(
                item = item,
                onClick = { onSelect(item) },
                modifier = Modifier
                    .padding(horizontal = CozySpacing.md)
                    .animateItem()
                    .cozyEntrance(CozyMotion.staggeredDelayMillis(index)),
            )
        }

        else -> item(key = "phrasebook_shelf") {
            PostcardShelf(history = history, onSelect = onSelect, modifier = Modifier.animateItem())
        }
    }
}

/** The shared VM maps a missing DB id to -1; fall back to the position so keys never collide. */
private fun UiHistoryItem.stableKey(index: Int): String = id?.takeIf { it >= 0 }?.let { "id$it" } ?: "pos$index"

@Composable
private fun PhrasebookHeader(
    count: Int,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CozyTheme.colors
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.saved_postcards),
            style = CozyText.rounded(CozyTextSize.headline),
            color = colors.inkPrimary,
        )
        if (count > 0) {
            Spacer(Modifier.size(8.dp))
            AnimatedContent(targetState = count, label = "historyCount") { value ->
                Text(
                    text = value.toString(),
                    style = CozyText.sans(CozyTextSize.caption2, FontWeight.Bold),
                    color = colors.indigo,
                    modifier = Modifier
                        .testTag(TranslateScreenTestTags.HISTORY_COUNT)
                        .clip(CircleShape)
                        .background(colors.indigoSoft)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (count > 1) {
            Row(
                modifier = Modifier
                    .cozyClickable(onClick = onToggleExpanded)
                    .clip(CircleShape)
                    .background(colors.indigoSoft.copy(alpha = 0.75f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ViewCarousel else Icons.AutoMirrored.Rounded.List,
                    contentDescription = null,
                    tint = colors.indigo,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(if (isExpanded) R.string.shelf else R.string.see_all),
                    style = CozyText.sans(CozyTextSize.caption, FontWeight.SemiBold),
                    color = colors.indigo,
                )
            }
        }
    }
}

@Composable
private fun EmptyShelf(modifier: Modifier = Modifier) {
    val colors = CozyTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .cozyCard(cornerRadius = 22.dp)
            .padding(CozySpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.indigoSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = colors.indigo, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.size(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                stringResource(R.string.empty_shelf_title),
                style = CozyText.sans(CozyTextSize.subheadline, FontWeight.SemiBold),
                color = colors.inkPrimary,
            )
            Text(
                stringResource(R.string.empty_shelf_message),
                style = CozyText.sans(CozyTextSize.caption),
                color = colors.inkSecondary,
            )
        }
    }
}

@Composable
private fun PostcardShelf(
    history: List<UiHistoryItem>,
    onSelect: (UiHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .testTag(TranslateScreenTestTags.POSTCARD_SHELF),
        contentPadding = PaddingValues(horizontal = CozySpacing.md, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        flingBehavior = rememberSnapFlingBehavior(listState, SnapPosition.Start),
    ) {
        itemsIndexed(history, key = { index, item -> "postcard_${item.stableKey(index)}" }) { index, item ->
            // Cards that are partly scrolled out shrink and fade slightly, like iOS scrollTransition.
            val isFullyVisible by remember(listState, index) {
                derivedStateOf {
                    val info = listState.layoutInfo
                    val itemInfo = info.visibleItemsInfo.firstOrNull { it.index == index }
                    itemInfo != null &&
                        itemInfo.offset >= info.viewportStartOffset &&
                        itemInfo.offset + itemInfo.size <= info.viewportEndOffset
                }
            }
            val scale by animateFloatAsState(if (isFullyVisible) 1f else 0.95f, CozyMotion.cozy(), label = "cardScale")
            val alpha by animateFloatAsState(if (isFullyVisible) 1f else 0.85f, CozyMotion.cozy(), label = "cardAlpha")
            TranslateHistoryItem(
                item = item,
                isPostcardShelf = true,
                onClick = { onSelect(item) },
                modifier = Modifier
                    .animateItem()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
            )
        }
    }
}

@Composable
private fun BottomCommandDock(
    state: TranslateState,
    onEvent: (TranslateEvent) -> Unit,
    onOpenVoice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CozyTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .cozyFloatingChrome(CircleShape)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LanguageDropDown(
                language = state.fromLanguage,
                isOpen = state.isChoosingFromLanguage,
                onClick = { onEvent(TranslateEvent.OpenFromLanguageDropDown) },
                onDismiss = { onEvent(TranslateEvent.StopChoosingLanguage) },
                onLanguageSelected = { onEvent(TranslateEvent.ChooseFromLanguage(it)) },
                contentDescription = stringResource(R.string.cd_choose_from_language, state.fromLanguage.language.languageName),
                modifier = Modifier
                    .weight(1f)
                    .testTag(TranslateScreenTestTags.FROM_LANGUAGE),
            )
            SwapLanguageButton(
                size = 34.dp,
                onClick = { onEvent(TranslateEvent.SwapLanguages) },
            )
            LanguageDropDown(
                language = state.toLanguage,
                isOpen = state.isChoosingToLanguage,
                onClick = { onEvent(TranslateEvent.OpenToLanguageDropDown) },
                onDismiss = { onEvent(TranslateEvent.StopChoosingLanguage) },
                onLanguageSelected = { onEvent(TranslateEvent.ChooseToLanguage(it)) },
                contentDescription = stringResource(R.string.cd_choose_to_language, state.toLanguage.language.languageName),
                modifier = Modifier
                    .weight(1f)
                    .testTag(TranslateScreenTestTags.TO_LANGUAGE),
            )
        }

        Box(
            modifier = Modifier
                .cozyClickable(onClick = onOpenVoice)
                .size(50.dp)
                .cozyFloatingChrome(CircleShape, tint = colors.indigo),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = stringResource(R.string.open_voice_translator),
                tint = colors.onIndigo,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Preview
@Composable
private fun TranslateScreenPreview() {
    CozyTheme(darkTheme = false) {
        TranslateScreen(
            state = TranslateState(
                history = listOf(
                    UiHistoryItem(
                        id = 1,
                        fromText = "Break a leg tonight!",
                        toText = "¡Mucha suerte esta noche!",
                        fromLanguage = UiLanguage.byCode("en"),
                        toLanguage = UiLanguage.byCode("es"),
                    ),
                ),
            ),
            settings = UserSettings(),
            onEvent = {},
            onOpenSettings = {},
            hourOfDay = 9,
        )
    }
}
