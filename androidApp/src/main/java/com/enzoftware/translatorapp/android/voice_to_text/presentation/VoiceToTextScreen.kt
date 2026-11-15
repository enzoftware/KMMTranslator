package com.enzoftware.translatorapp.android.voice_to_text.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.enzoftware.translatorapp.android.R
import com.enzoftware.translatorapp.android.core.designsystem.CozyAmbientBackground
import com.enzoftware.translatorapp.android.core.designsystem.CozyChromeIconButton
import com.enzoftware.translatorapp.android.core.designsystem.CozyCopyChip
import com.enzoftware.translatorapp.android.core.designsystem.CozyMotion
import com.enzoftware.translatorapp.android.core.designsystem.CozySpacing
import com.enzoftware.translatorapp.android.core.designsystem.CozyText
import com.enzoftware.translatorapp.android.core.designsystem.CozyTextSize
import com.enzoftware.translatorapp.android.core.designsystem.CozyTheme
import com.enzoftware.translatorapp.android.core.designsystem.cozyCard
import com.enzoftware.translatorapp.android.core.designsystem.cozyClickable
import com.enzoftware.translatorapp.android.core.designsystem.cozyFloatingChrome
import com.enzoftware.translatorapp.android.voice_to_text.presentation.components.VoiceRecorderButton
import com.enzoftware.translatorapp.android.voice_to_text.presentation.components.VoiceRecorderDisplay
import com.enzoftware.translatorapp.core.presentation.UiLanguage
import com.enzoftware.translatorapp.voice_to_text.presentation.DisplayState
import com.enzoftware.translatorapp.voice_to_text.presentation.VoiceToTextEvent
import com.enzoftware.translatorapp.voice_to_text.presentation.VoiceToTextState

object VoiceToTextTestTags {
    const val STATUS = "voice_status"
    const val SPOKEN_TEXT = "voice_spoken_text"
}

/**
 * Stateful entry point: owns the RECORD_AUDIO permission flow and forwards everything else to
 * the stateless [VoiceToTextScreen].
 */
@Composable
fun VoiceToTextRoute(
    state: VoiceToTextState,
    languageCode: String,
    onResult: (String) -> Unit,
    onEvent: (VoiceToTextEvent) -> Unit,
) {
    val context = LocalContext.current
    val isSpeechAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    val permissionMemory = remember { MicPermissionMemory(context) }
    // Saveable so a rotation neither forgets the diagnosis nor fires the permission dialog again.
    var micPermission by rememberSaveable { mutableStateOf(context.currentMicPermission()) }
    var hasAutoRequested by rememberSaveable { mutableStateOf(false) }
    var rationaleBeforeRequest by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val rationaleAfter = context.shouldShowMicRationale()
        micPermission = classifyMicPermissionResult(
            granted = granted,
            rationaleBeforeRequest = rationaleBeforeRequest,
            rationaleAfterRequest = rationaleAfter,
            deniedBefore = permissionMemory.deniedBefore,
        )
        permissionMemory.deniedBefore = !granted && (rationaleAfter || permissionMemory.deniedBefore ||
            micPermission == MicPermission.BLOCKED)
        onEvent(VoiceToTextEvent.PermissionResult(isGranted = granted, isPermanentDeclined = micPermission == MicPermission.BLOCKED))
    }
    val requestPermission = {
        rationaleBeforeRequest = context.shouldShowMicRationale()
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(Unit) {
        when {
            micPermission == MicPermission.GRANTED ->
                onEvent(VoiceToTextEvent.PermissionResult(isGranted = true, isPermanentDeclined = false))

            !hasAutoRequested -> {
                hasAutoRequested = true
                requestPermission()
            }
        }
    }

    // Coming back from system settings: pick up a newly granted permission.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (micPermission != MicPermission.GRANTED && context.currentMicPermission() == MicPermission.GRANTED) {
            micPermission = MicPermission.GRANTED
            onEvent(VoiceToTextEvent.Reset)
            onEvent(VoiceToTextEvent.PermissionResult(isGranted = true, isPermanentDeclined = false))
        }
    }

    VoiceToTextScreen(
        state = state,
        languageCode = languageCode,
        diagnostics = VoicePermissionDiagnostics(
            microphone = micPermission,
            isSpeechRecognitionAvailable = isSpeechAvailable,
            errorText = state.recordErrorText.takeIf { micPermission == MicPermission.GRANTED },
        ),
        onResult = onResult,
        onEvent = onEvent,
        onRequestPermission = requestPermission,
        onOpenSettings = { context.openAppSettings() },
    )
}

@Composable
fun VoiceToTextScreen(
    state: VoiceToTextState,
    languageCode: String,
    diagnostics: VoicePermissionDiagnostics,
    onResult: (String) -> Unit,
    onEvent: (VoiceToTextEvent) -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val shake = remember { Animatable(0f) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var errorFeedbackTrigger by remember { mutableIntStateOf(0) }
    val languageName = remember(languageCode) { UiLanguage.byCode(languageCode).language.languageName }
    val displayState = when {
        // Without the permission the shared VM reports ERROR; show the permission explainer instead.
        diagnostics.hasMissingPermission -> DisplayState.WAITING_TO_TALK
        else -> state.displayState ?: DisplayState.WAITING_TO_TALK
    }

    LaunchedEffect(errorFeedbackTrigger) {
        if (errorFeedbackTrigger == 0) return@LaunchedEffect
        haptics.performHapticFeedback(HapticFeedbackType.Reject)
        shake.animateTo(-7f, tween(60))
        shake.animateTo(7f, tween(60))
        shake.animateTo(0f, CozyMotion.snappy())
    }
    LaunchedEffect(displayState) {
        if (displayState == DisplayState.ERROR) errorFeedbackTrigger++
    }

    fun handleRecordTap(forceRetry: Boolean = false) {
        when {
            displayState == DisplayState.DISPLAYING_RESULTS && !forceRetry -> onResult(state.spokenText)
            diagnostics.issue == VoiceIssue.MIC_NEEDED -> onRequestPermission()
            diagnostics.needsSettingsRedirect -> {
                errorFeedbackTrigger++
                showSettingsDialog = true
            }
            else -> onEvent(VoiceToTextEvent.ToggleRecording(languageCode))
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        CozyAmbientBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(start = CozySpacing.md, end = CozySpacing.md, top = CozySpacing.sm, bottom = CozySpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CozySpacing.lg),
        ) {
            TopBar(
                displayState = displayState,
                languageName = languageName,
                onClose = { onEvent(VoiceToTextEvent.Close) },
            )

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val minHeight = maxHeight
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = minHeight),
                    verticalArrangement = Arrangement.Center,
                ) {
                    AnimatedContent(
                        targetState = displayState,
                        transitionSpec = {
                            (fadeIn(CozyMotion.cozy()) + scaleIn(CozyMotion.cozy(), initialScale = 0.96f)) togetherWith
                                fadeOut(CozyMotion.gentle())
                        },
                        label = "voiceStage",
                    ) { stage ->
                        MainStage(
                            stage = stage,
                            state = state,
                            diagnostics = diagnostics,
                            languageName = languageName,
                            shakeOffset = shake.value,
                            onPrimaryPermissionAction = {
                                if (diagnostics.needsSettingsRedirect) onOpenSettings() else onRequestPermission()
                            },
                            onOpenSettings = onOpenSettings,
                            onRetry = { handleRecordTap() },
                        )
                    }
                }
            }

            BottomDock(
                displayState = displayState,
                livePower = state.powerRatios.lastOrNull() ?: 0f,
                onRecordTap = { handleRecordTap() },
                onRecordAgain = { handleRecordTap(forceRetry = true) },
            )
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(stringResource(R.string.voice_permission_dialog_title)) },
            text = { Text(diagnostics.detailMessage(languageName)) },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    onOpenSettings()
                }) { Text(stringResource(R.string.open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
            containerColor = CozyTheme.colors.paperElevated,
        )
    }
}

@Composable
private fun TopBar(
    displayState: DisplayState,
    languageName: String,
    onClose: () -> Unit,
) {
    val colors = CozyTheme.colors
    val isSpeaking = displayState == DisplayState.SPEAKING
    val pulse by rememberInfiniteTransition(label = "statusPulse").animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot",
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        CozyChromeIconButton(
            icon = Icons.Rounded.KeyboardArrowDown,
            contentDescription = stringResource(R.string.cd_close_voice),
            onClick = onClose,
        )
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier
                .cozyFloatingChrome(CircleShape)
                .padding(horizontal = 14.dp, vertical = 9.dp)
                .testTag(VoiceToTextTestTags.STATUS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer { alpha = if (isSpeaking) pulse else 1f }
                    .clip(CircleShape)
                    .background(if (isSpeaking) colors.terracotta else colors.indigo),
            )
            Text(
                text = stringResource(if (isSpeaking) R.string.listening_in else R.string.voice_in, languageName),
                style = CozyText.sans(CozyTextSize.caption, FontWeight.SemiBold),
                color = colors.inkPrimary,
            )
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun MainStage(
    stage: DisplayState,
    state: VoiceToTextState,
    diagnostics: VoicePermissionDiagnostics,
    languageName: String,
    shakeOffset: Float,
    onPrimaryPermissionAction: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
) {
    val colors = CozyTheme.colors
    when (stage) {
        DisplayState.WAITING_TO_TALK -> {
            val missing = diagnostics.hasMissingPermission || !diagnostics.isSpeechRecognitionAvailable
            val pulse by rememberInfiniteTransition(label = "waitPulse").animateFloat(
                initialValue = 1f,
                targetValue = 0.55f,
                animationSpec = infiniteRepeatable(tween(1_200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "waitIcon",
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .cozyCard(elevated = true)
                    .padding(CozySpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CozySpacing.md),
            ) {
                StageIcon(
                    icon = if (missing) Icons.Rounded.MicOff else Icons.Rounded.GraphicEq,
                    tint = if (missing) colors.terracotta else colors.indigo,
                    background = if (missing) colors.terracotta.copy(alpha = 0.16f) else colors.indigoSoft,
                    size = 76,
                    iconSize = 36,
                    iconAlpha = if (missing) 1f else pulse,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (missing) stringResource(diagnostics.headlineRes) else stringResource(R.string.voice_ready_title),
                        style = CozyText.rounded(CozyTextSize.title2),
                        color = colors.inkPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = if (missing) {
                            diagnostics.detailMessage(languageName)
                        } else {
                            stringResource(R.string.voice_ready_message, languageName)
                        },
                        style = CozyText.sans(CozyTextSize.subheadline),
                        color = colors.inkSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
                if (missing) {
                    PermissionStatusPills(diagnostics)
                    PrimaryCapsuleButton(
                        text = stringResource(
                            if (diagnostics.needsSettingsRedirect) R.string.open_settings else R.string.allow_microphone,
                        ),
                        icon = if (diagnostics.needsSettingsRedirect) Icons.Rounded.Settings else Icons.Rounded.Mic,
                        onClick = onPrimaryPermissionAction,
                    )
                }
            }
        }

        DisplayState.SPEAKING -> Column(verticalArrangement = Arrangement.spacedBy(CozySpacing.md), horizontalAlignment = Alignment.CenterHorizontally) {
            VoiceRecorderDisplay(
                powerRatios = state.powerRatios,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            )
            if (state.spokenText.isNotEmpty()) {
                Text(
                    text = state.spokenText,
                    style = CozyText.sans(CozyTextSize.title3, FontWeight.Medium),
                    color = colors.inkPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .cozyCard(cornerRadius = 20.dp)
                        .padding(CozySpacing.md),
                )
            } else {
                Text(
                    text = stringResource(R.string.listening_carefully),
                    style = CozyText.sans(CozyTextSize.subheadline, FontWeight.Medium),
                    color = colors.inkSecondary,
                )
            }
        }

        DisplayState.DISPLAYING_RESULTS -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .cozyCard(elevated = true)
                .padding(CozySpacing.lg),
            verticalArrangement = Arrangement.spacedBy(CozySpacing.md),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.sage.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Rounded.FormatQuote, contentDescription = null, tint = colors.sage, modifier = Modifier.size(13.dp))
                    Text(
                        stringResource(R.string.captured_speech),
                        style = CozyText.sans(CozyTextSize.caption, FontWeight.SemiBold),
                        color = colors.sage,
                    )
                }
                Spacer(Modifier.weight(1f))
                CozyCopyChip(text = state.spokenText)
            }
            SelectionContainer {
                Text(
                    text = state.spokenText,
                    style = CozyText.sans(CozyTextSize.title3, FontWeight.Medium),
                    color = colors.inkPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(VoiceToTextTestTags.SPOKEN_TEXT),
                )
            }
            Text(
                stringResource(R.string.voice_result_hint),
                style = CozyText.sans(CozyTextSize.caption),
                color = colors.inkMuted,
            )
        }

        DisplayState.ERROR -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(shakeOffset.dp.roundToPx(), 0) }
                .cozyCard(elevated = true)
                .padding(CozySpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CozySpacing.md),
        ) {
            StageIcon(
                icon = if (diagnostics.needsSettingsRedirect) Icons.Rounded.Lock else Icons.Rounded.MicOff,
                tint = colors.danger,
                background = colors.danger.copy(alpha = 0.14f),
                size = 64,
                iconSize = 28,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(diagnostics.headlineRes),
                    style = CozyText.rounded(CozyTextSize.title3),
                    color = colors.inkPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = diagnostics.detailMessage(languageName),
                    style = CozyText.sans(CozyTextSize.subheadline),
                    color = colors.inkSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            if (diagnostics.needsSettingsRedirect) PermissionStatusPills(diagnostics)
            Column(
                modifier = Modifier.padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (diagnostics.needsSettingsRedirect) {
                    PrimaryCapsuleButton(stringResource(R.string.open_settings), Icons.Rounded.Settings, onOpenSettings)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cozyClickable(onClick = onRetry)
                        .clip(CircleShape)
                        .background(colors.indigoSoft)
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = colors.indigo, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(
                        stringResource(R.string.try_again),
                        style = CozyText.sans(CozyTextSize.subheadline, FontWeight.SemiBold),
                        color = colors.indigo,
                    )
                }
            }
        }
    }
}

@Composable
private fun VoicePermissionDiagnostics.detailMessage(languageName: String): String = when (issue) {
    VoiceIssue.MIC_BLOCKED -> stringResource(R.string.voice_mic_blocked_message)
    VoiceIssue.MIC_NEEDED -> stringResource(R.string.voice_mic_needed_message)
    VoiceIssue.SPEECH_UNAVAILABLE -> stringResource(R.string.voice_speech_unavailable_message, languageName)
    VoiceIssue.RECORDING_ERROR -> errorText ?: stringResource(R.string.voice_error_message)
    VoiceIssue.NONE -> stringResource(R.string.voice_error_message)
}

@Composable
private fun StageIcon(
    icon: ImageVector,
    tint: Color,
    background: Color,
    size: Int,
    iconSize: Int,
    iconAlpha: Float = 1f,
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(iconSize.dp)
                .graphicsLayer { alpha = iconAlpha },
        )
    }
}

@Composable
private fun PermissionStatusPills(diagnostics: VoicePermissionDiagnostics) {
    val colors = CozyTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PermissionPill(
            text = stringResource(R.string.permission_mic, stringResource(diagnostics.microphone.labelRes)),
            icon = Icons.Rounded.Mic,
            color = when (diagnostics.microphone) {
                MicPermission.GRANTED -> colors.sage
                MicPermission.DENIED, MicPermission.BLOCKED -> colors.danger
                MicPermission.NOT_ASKED -> colors.inkMuted
            },
        )
        PermissionPill(
            text = stringResource(R.string.permission_speech, stringResource(diagnostics.speechLabelRes)),
            icon = Icons.Rounded.GraphicEq,
            color = if (diagnostics.isSpeechRecognitionAvailable) colors.sage else colors.danger,
        )
    }
}

@Composable
private fun PermissionPill(text: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Text(text, style = CozyText.sans(CozyTextSize.caption2, FontWeight.SemiBold), color = color)
    }
}

@Composable
private fun PrimaryCapsuleButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    val colors = CozyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cozyClickable(onClick = onClick)
            .clip(CircleShape)
            .background(colors.indigo)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = colors.onIndigo, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(6.dp))
        Text(text, style = CozyText.sans(CozyTextSize.subheadline, FontWeight.SemiBold), color = colors.onIndigo)
    }
}

@Composable
private fun BottomDock(
    displayState: DisplayState,
    livePower: Float,
    onRecordTap: () -> Unit,
    onRecordAgain: () -> Unit,
) {
    val colors = CozyTheme.colors
    val showSecondary = displayState == DisplayState.DISPLAYING_RESULTS || displayState == DisplayState.ERROR
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Mirror spacer keeps the record button centred when the secondary button shows.
        if (showSecondary) Spacer(Modifier.size(52.dp))
        VoiceRecorderButton(displayState = displayState, livePower = livePower, onClick = onRecordTap)
        AnimatedVisibility(visible = showSecondary, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
            Box(
                modifier = Modifier
                    .cozyClickable(onClick = onRecordAgain)
                    .size(52.dp)
                    .cozyFloatingChrome(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.record_again),
                    tint = colors.inkPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

private fun Context.currentMicPermission(): MicPermission =
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
        MicPermission.GRANTED
    } else {
        MicPermission.NOT_ASKED
    }

private fun Context.shouldShowMicRationale(): Boolean =
    findActivity()?.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) ?: false

/** Survives app restarts so a permanently blocked mic is recognised on the next visit too. */
private class MicPermissionMemory(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("voice_permissions", Context.MODE_PRIVATE)

    var deniedBefore: Boolean
        get() = prefs.getBoolean(KEY_DENIED_BEFORE, false)
        set(value) = prefs.edit().putBoolean(KEY_DENIED_BEFORE, value).apply()

    private companion object {
        const val KEY_DENIED_BEFORE = "mic_denied_before"
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
