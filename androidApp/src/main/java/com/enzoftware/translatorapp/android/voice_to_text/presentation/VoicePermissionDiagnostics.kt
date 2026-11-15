package com.enzoftware.translatorapp.android.voice_to_text.presentation

import androidx.annotation.StringRes
import com.enzoftware.translatorapp.android.R

enum class MicPermission(@StringRes val labelRes: Int) {
    GRANTED(R.string.permission_allowed),
    NOT_ASKED(R.string.permission_not_asked),
    DENIED(R.string.permission_denied),

    /** Denied with "don't ask again": only the system settings screen can grant it now. */
    BLOCKED(R.string.permission_blocked),
}

/**
 * Interprets a RECORD_AUDIO request result. Android reports "dismissed the dialog" and "blocked
 * forever" identically (denied, no rationale), so the rationale flag from before the request and a
 * remembered earlier denial are used to tell them apart:
 *
 * - rationale after the request: the user tapped "Don't allow" but can still be asked again.
 * - no rationale now, but there was one before (or an earlier denial was seen): that denial made it
 *   permanent, or the system auto-denied without showing a dialog.
 * - otherwise the first dialog was just dismissed, so nothing is blocked yet.
 */
fun classifyMicPermissionResult(
    granted: Boolean,
    rationaleBeforeRequest: Boolean,
    rationaleAfterRequest: Boolean,
    deniedBefore: Boolean,
): MicPermission = when {
    granted -> MicPermission.GRANTED
    rationaleAfterRequest -> MicPermission.DENIED
    rationaleBeforeRequest || deniedBefore -> MicPermission.BLOCKED
    else -> MicPermission.DENIED
}

enum class VoiceIssue {
    NONE,
    MIC_NEEDED,
    MIC_BLOCKED,
    SPEECH_UNAVAILABLE,
    RECORDING_ERROR,
}

/**
 * Android counterpart of the iOS `VoicePermissionDiagnostics`: decides which problem (if any) the
 * voice screen should explain, and whether the fix lives in system settings.
 */
data class VoicePermissionDiagnostics(
    val microphone: MicPermission,
    val isSpeechRecognitionAvailable: Boolean,
    val errorText: String? = null,
) {
    val hasMissingPermission: Boolean
        get() = microphone != MicPermission.GRANTED

    val issue: VoiceIssue
        get() = when {
            microphone == MicPermission.BLOCKED -> VoiceIssue.MIC_BLOCKED
            microphone != MicPermission.GRANTED -> VoiceIssue.MIC_NEEDED
            !isSpeechRecognitionAvailable -> VoiceIssue.SPEECH_UNAVAILABLE
            errorText != null -> VoiceIssue.RECORDING_ERROR
            else -> VoiceIssue.NONE
        }

    val needsSettingsRedirect: Boolean
        get() = issue == VoiceIssue.MIC_BLOCKED || issue == VoiceIssue.SPEECH_UNAVAILABLE

    @get:StringRes
    val headlineRes: Int
        get() = when (issue) {
            VoiceIssue.MIC_BLOCKED -> R.string.voice_mic_blocked_title
            VoiceIssue.MIC_NEEDED -> R.string.voice_mic_needed_title
            VoiceIssue.SPEECH_UNAVAILABLE -> R.string.voice_speech_unavailable_title
            VoiceIssue.RECORDING_ERROR, VoiceIssue.NONE -> R.string.voice_error_title
        }

    @get:StringRes
    val speechLabelRes: Int
        get() = if (isSpeechRecognitionAvailable) R.string.speech_available else R.string.speech_unavailable
}
