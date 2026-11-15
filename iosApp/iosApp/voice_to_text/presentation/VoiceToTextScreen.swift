//
//  VoiceToTextScreen.swift
//  iosApp
//
//  Created by Enzo Lizama on 24/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import AVFoundation
import Speech
import SwiftUI
import UIKit
import shared

struct VoicePermissionDiagnostics: Equatable {
    enum PermissionState: Equatable {
        case granted
        case denied
        case restricted
        case undetermined

        var label: String {
            switch self {
            case .granted:
                return "Allowed"
            case .denied:
                return "Off in Settings"
            case .restricted:
                return "Restricted"
            case .undetermined:
                return "Not asked"
            }
        }

        var isBlocked: Bool {
            self == .denied || self == .restricted
        }
    }

    let microphone: PermissionState
    let speechRecognition: PermissionState
    let errorText: String?
    let languageDisplayName: String

    var hasMissingPermission: Bool {
        microphone.isBlocked || speechRecognition.isBlocked
    }

    var isRecognizerUnavailableError: Bool {
        guard let errorText else { return false }
        let lower = errorText.lowercased()
        return lower.contains("not available") || lower.contains("unavailable")
    }

    var isAudioHardwareError: Bool {
        guard let errorText else { return false }
        let lower = errorText.lowercased()
        return lower.contains("audio format") || lower.contains("input node")
    }

    var needsSettingsRedirect: Bool {
        if hasMissingPermission || isRecognizerUnavailableError {
            return true
        }
        guard let errorText else { return false }
        return errorText.localizedCaseInsensitiveContains("permission")
    }

    var headline: String {
        if microphone.isBlocked && speechRecognition.isBlocked {
            return "Microphone & Speech access are off"
        }
        if microphone.isBlocked || (errorText?.localizedCaseInsensitiveContains("record your voice") ?? false) {
            return "Microphone access is off"
        }
        if speechRecognition.isBlocked || (errorText?.localizedCaseInsensitiveContains("transcribe audio") ?? false) {
            return "Speech Recognition is off"
        }
        if isRecognizerUnavailableError {
            return "Speech recognition unavailable"
        }
        if isAudioHardwareError {
            return "Microphone unavailable here"
        }
        return "Couldn't capture audio"
    }

    var detailMessage: String {
        if microphone.isBlocked && speechRecognition.isBlocked {
            return "Parley needs Microphone and Speech Recognition permissions to listen and transcribe. Enable both in iOS Settings."
        }
        if microphone.isBlocked || (errorText?.localizedCaseInsensitiveContains("record your voice") ?? false) {
            return "Parley needs Microphone access to hear your voice. Enable Microphone in Settings, then return here to record."
        }
        if speechRecognition.isBlocked || (errorText?.localizedCaseInsensitiveContains("transcribe audio") ?? false) {
            return "Parley needs Speech Recognition access to transcribe your speech. Enable it in Settings to continue."
        }
        if isRecognizerUnavailableError {
            return "Speech recognition isn't available right now for \(languageDisplayName). Check that Siri & Dictation are enabled in Settings or verify your connection."
        }
        if isAudioHardwareError {
            return "No active microphone input format was detected (common on simulators without audio routing). Try again on a physical iPhone."
        }
        return errorText ?? "Something went wrong while starting voice input. Please try again."
    }

    static func current(
        errorText: String? = nil,
        languageDisplayName: String = "English"
    ) -> VoicePermissionDiagnostics {
        let micState: PermissionState
        if #available(iOS 17.0, *) {
            switch AVAudioApplication.shared.recordPermission {
            case .granted:
                micState = .granted
            case .denied:
                micState = .denied
            case .undetermined:
                micState = .undetermined
            @unknown default:
                micState = .undetermined
            }
        } else {
            switch AVAudioSession.sharedInstance().recordPermission {
            case .granted:
                micState = .granted
            case .denied:
                micState = .denied
            case .undetermined:
                micState = .undetermined
            @unknown default:
                micState = .undetermined
            }
        }

        let speechState: PermissionState
        switch SFSpeechRecognizer.authorizationStatus() {
        case .authorized:
            speechState = .granted
        case .denied:
            speechState = .denied
        case .restricted:
            speechState = .restricted
        case .notDetermined:
            speechState = .undetermined
        @unknown default:
            speechState = .undetermined
        }

        return VoicePermissionDiagnostics(
            microphone: micState,
            speechRecognition: speechState,
            errorText: errorText,
            languageDisplayName: languageDisplayName
        )
    }
}

struct VoiceToTextScreen: View {
    private let onResult: (String) -> Void

    @StateObject var viewModel: IOSVoiceToTextViewModel
    private let voiceToTextParser: any VoiceToTextParser
    private let languageCode: String

    @Environment(\.dismiss) private var dismiss
    @Environment(\.openURL) private var openURL
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    @State private var diagnostics: VoicePermissionDiagnostics
    @State private var showSettingsAlert = false
    @State private var errorFeedbackTrigger = 0
    @State private var shakeOffset: CGFloat = 0

    init(
        onResult: @escaping (String) -> Void,
        voiceToTextParser: any VoiceToTextParser,
        languageCode: String
    ) {
        self.onResult = onResult
        self.voiceToTextParser = voiceToTextParser
        self.languageCode = languageCode
        self._viewModel = StateObject(
            wrappedValue: IOSVoiceToTextViewModel(
                voiceToTextParser: voiceToTextParser,
                languageCode: languageCode
            )
        )
        let displayName = UiLanguage.companion.byCode(code: languageCode).language.languageName
        self._diagnostics = State(
            initialValue: VoicePermissionDiagnostics.current(languageDisplayName: displayName)
        )
    }

    private var currentDisplayState: DisplayState {
        viewModel.state.displayState ?? .waitingToTalk
    }

    private var languageDisplayName: String {
        UiLanguage.companion.byCode(code: languageCode).language.languageName
    }

    private var latestPower: Double {
        guard let last = viewModel.state.powerRatios.last else { return 0 }
        return Double(truncating: last)
    }

    var body: some View {
        ZStack {
            CozyAmbientBackground()

            VStack(spacing: CozyTheme.Spacing.lg) {
                topBar

                GeometryReader { proxy in
                    ScrollView {
                        mainStage
                            .id(currentDisplayState)
                            .transition(.blurReplace)
                            .frame(maxWidth: .infinity, minHeight: proxy.size.height)
                    }
                    .scrollIndicators(.hidden)
                }

                bottomDock
            }
            .padding(.horizontal, CozyTheme.Spacing.md)
            .padding(.top, CozyTheme.Spacing.sm)
            .padding(.bottom, CozyTheme.Spacing.lg)
        }
        .animation(CozyTheme.Motion.cozySpring, value: currentDisplayState)
        .toolbar(.hidden, for: .navigationBar)
        .sensoryFeedback(.error, trigger: errorFeedbackTrigger)
        .alert("Voice Permissions Required", isPresented: $showSettingsAlert) {
            Button("Open Settings") {
                openAppSettings()
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text(diagnostics.detailMessage)
        }
        .onAppear {
            refreshDiagnostics()
            viewModel.startObserving()
        }
        .onDisappear {
            viewModel.dispose()
        }
        .onChange(of: scenePhase) { _, newPhase in
            guard newPhase == .active else { return }
            refreshDiagnostics()
            if !diagnostics.hasMissingPermission && currentDisplayState == .error {
                viewModel.onEvent(event: VoiceToTextEvent.Reset())
                viewModel.startObserving()
            }
        }
        .onChange(of: viewModel.state.recordErrorText) { _, newValue in
            refreshDiagnostics(errorText: newValue)
        }
        .onChange(of: currentDisplayState) { _, _ in
            refreshDiagnostics(errorText: viewModel.state.recordErrorText)
        }
    }

    private var topBar: some View {
        CozyGlassContainer(spacing: 12) {
            HStack {
                Button {
                    dismiss()
                } label: {
                    Image(systemName: "chevron.down")
                        .font(.subheadline.weight(.bold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                        .frame(width: 40, height: 40)
                        .cozyFloatingChrome(in: Circle(), interactive: false)
                        .contentShape(Circle())
                }
                .buttonStyle(CozyScaleButtonStyle())
                .accessibilityLabel("Close voice input")

                Spacer()

                HStack(spacing: 8) {
                    Circle()
                        .fill(
                            currentDisplayState == .speaking
                                ? CozyTheme.Colors.terracotta
                                : CozyTheme.Colors.indigo
                        )
                        .frame(width: 8, height: 8)
                        .symbolEffect(.pulse, isActive: currentDisplayState == .speaking)

                    Text(
                        currentDisplayState == .speaking
                            ? "Listening in \(languageDisplayName)"
                            : "Voice · \(languageDisplayName)"
                    )
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 9)
                .cozyFloatingChrome(in: Capsule(), interactive: false)

                Spacer()

                Color.clear
                    .frame(width: 40, height: 40)
            }
        }
    }

    @ViewBuilder
    private var mainStage: some View {
        switch currentDisplayState {
        case .waitingToTalk:
            VStack(spacing: CozyTheme.Spacing.md) {
                ZStack {
                    Circle()
                        .fill(
                            diagnostics.hasMissingPermission
                                ? CozyTheme.Colors.terracotta.opacity(0.16)
                                : CozyTheme.Colors.indigoSoft
                        )
                        .frame(width: 76, height: 76)

                    Image(
                        systemName: diagnostics.hasMissingPermission
                            ? "mic.badge.xmark"
                            : "waveform.circle.fill"
                    )
                    .font(.system(size: 36))
                    .foregroundStyle(
                        diagnostics.hasMissingPermission
                            ? CozyTheme.Colors.terracotta
                            : CozyTheme.Colors.indigo
                    )
                    .symbolEffect(.pulse.byLayer, isActive: !diagnostics.hasMissingPermission)
                }

                VStack(spacing: 6) {
                    Text(
                        diagnostics.hasMissingPermission
                            ? diagnostics.headline
                            : "Ready when you are"
                    )
                    .font(.system(.title2, design: .rounded, weight: .bold))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)
                    .multilineTextAlignment(.center)

                    Text(
                        diagnostics.hasMissingPermission
                            ? diagnostics.detailMessage
                            : "Tap the microphone below and start speaking in \(languageDisplayName)."
                    )
                    .font(.subheadline)
                    .foregroundStyle(CozyTheme.Colors.inkSecondary)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
                }

                if diagnostics.hasMissingPermission {
                    permissionStatusPills

                    settingsRedirectButton
                }
            }
            .frame(maxWidth: .infinity)
            .padding(CozyTheme.Spacing.xl)
            .cozyCard(cornerRadius: CozyTheme.Radius.card, isElevated: true)

        case .speaking:
            VStack(spacing: CozyTheme.Spacing.md) {
                VoiceRecorderDisplay(
                    powerRatios: viewModel.state.powerRatios.map { Double(truncating: $0) }
                )
                .frame(height: 120)

                if !viewModel.state.spokenText.isEmpty {
                    Text(viewModel.state.spokenText)
                        .font(.title3.weight(.medium))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                        .multilineTextAlignment(.center)
                        .padding(CozyTheme.Spacing.md)
                        .frame(maxWidth: .infinity)
                        .cozyCard(cornerRadius: 20)
                        .transition(.blurReplace)
                } else {
                    Text("Listening carefully...")
                        .font(.subheadline.weight(.medium))
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                }
            }

        case .displayingResults:
            VStack(alignment: .leading, spacing: CozyTheme.Spacing.md) {
                HStack {
                    Label("Captured speech", systemImage: "quote.opening")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(CozyTheme.Colors.sage)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(
                            Capsule()
                                .fill(CozyTheme.Colors.sage.opacity(0.14))
                        )

                    Spacer()

                    CozyCopyChip(text: viewModel.state.spokenText)
                }

                Text(viewModel.state.spokenText)
                    .font(.title3.weight(.medium))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .textSelection(.enabled)

                Text("Tap the checkmark to send this text to the translator.")
                    .font(.caption)
                    .foregroundStyle(CozyTheme.Colors.inkMuted)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(CozyTheme.Spacing.lg)
            .cozyCard(cornerRadius: CozyTheme.Radius.card, isElevated: true)

        case .error:
            VStack(spacing: CozyTheme.Spacing.md) {
                ZStack {
                    Circle()
                        .fill(CozyTheme.Colors.danger.opacity(0.14))
                        .frame(width: 64, height: 64)

                    Image(systemName: diagnostics.hasMissingPermission ? "lock.slash.fill" : "mic.slash.fill")
                        .font(.title2.weight(.semibold))
                        .foregroundStyle(CozyTheme.Colors.danger)
                }

                VStack(spacing: 6) {
                    Text(diagnostics.headline)
                        .font(.system(.title3, design: .rounded, weight: .bold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                        .multilineTextAlignment(.center)

                    Text(diagnostics.detailMessage)
                        .font(.subheadline)
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                        .multilineTextAlignment(.center)
                        .fixedSize(horizontal: false, vertical: true)
                }

                if diagnostics.needsSettingsRedirect {
                    permissionStatusPills
                }

                VStack(spacing: 10) {
                    if diagnostics.needsSettingsRedirect {
                        settingsRedirectButton
                    }

                    Button {
                        handleRecordTap()
                    } label: {
                        Label("Try Again", systemImage: "arrow.clockwise")
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(CozyTheme.Colors.indigo)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(
                                Capsule()
                                    .fill(CozyTheme.Colors.indigoSoft)
                            )
                            .contentShape(Capsule())
                    }
                    .buttonStyle(CozyScaleButtonStyle())
                }
                .padding(.top, 4)
            }
            .frame(maxWidth: .infinity)
            .padding(CozyTheme.Spacing.xl)
            .cozyCard(cornerRadius: CozyTheme.Radius.card, isElevated: true)
            .offset(x: shakeOffset)

        default:
            EmptyView()
        }
    }

    private var permissionStatusPills: some View {
        HStack(spacing: 8) {
            permissionPill(
                title: "Mic",
                icon: "mic.fill",
                state: diagnostics.microphone
            )
            permissionPill(
                title: "Speech",
                icon: "waveform",
                state: diagnostics.speechRecognition
            )
        }
    }

    private func permissionPill(
        title: String,
        icon: String,
        state: VoicePermissionDiagnostics.PermissionState
    ) -> some View {
        let pillColor: Color = switch state {
        case .granted: CozyTheme.Colors.sage
        case .denied, .restricted: CozyTheme.Colors.danger
        case .undetermined: CozyTheme.Colors.inkMuted
        }

        return HStack(spacing: 5) {
            Image(systemName: icon)
                .font(.caption2.weight(.bold))
            Text("\(title): \(state.label)")
                .font(.caption2.weight(.semibold))
        }
        .foregroundStyle(pillColor)
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .background(
            Capsule()
                .fill(pillColor.opacity(0.12))
        )
    }

    private var settingsRedirectButton: some View {
        Button {
            openAppSettings()
        } label: {
            Label("Open Settings", systemImage: "gearshape.fill")
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(CozyTheme.Colors.onIndigo)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(
                    Capsule()
                        .fill(CozyTheme.Colors.indigo)
                )
                .contentShape(Capsule())
        }
        .buttonStyle(CozyScaleButtonStyle())
    }

    private var bottomDock: some View {
        CozyGlassContainer(spacing: 16) {
            HStack(spacing: 20) {
                if currentDisplayState == .displayingResults || currentDisplayState == .error {
                    Color.clear
                        .frame(width: 52, height: 52)
                }

                VoiceRecorderButton(
                    displayState: currentDisplayState,
                    livePower: latestPower,
                    onClick: { handleRecordTap() }
                )

                if currentDisplayState == .displayingResults || currentDisplayState == .error {
                    Button {
                        handleRecordTap(forceRetry: true)
                    } label: {
                        Image(systemName: "arrow.clockwise")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(CozyTheme.Colors.inkPrimary)
                            .frame(width: 52, height: 52)
                            .cozyFloatingChrome(in: Circle(), interactive: false)
                            .contentShape(Circle())
                    }
                    .buttonStyle(CozyScaleButtonStyle())
                    .transition(.scale.combined(with: .opacity))
                    .accessibilityLabel("Record again")
                }
            }
        }
    }

    private func handleRecordTap(forceRetry: Bool = false) {
        refreshDiagnostics(errorText: viewModel.state.recordErrorText)

        if currentDisplayState == .displayingResults && !forceRetry {
            onResult(viewModel.state.spokenText)
            dismiss()
            return
        }

        if diagnostics.hasMissingPermission {
            if !viewModel.state.canRecord {
                viewModel.startObserving()
            }
            if currentDisplayState != .error {
                viewModel.onEvent(
                    event: VoiceToTextEvent.ToggleRecording(
                        languageCode: languageCode
                    )
                )
            }
            triggerErrorFeedback()
            showSettingsAlert = true
            return
        }

        if !viewModel.state.canRecord {
            viewModel.startObserving()
        }

        viewModel.onEvent(
            event: VoiceToTextEvent.ToggleRecording(
                languageCode: languageCode
            )
        )

        Task { @MainActor in
            try? await Task.sleep(for: .milliseconds(120))
            refreshDiagnostics(errorText: viewModel.state.recordErrorText)
            if currentDisplayState == .error {
                triggerErrorFeedback()
            }
        }
    }

    private func refreshDiagnostics(errorText: String? = nil) {
        diagnostics = VoicePermissionDiagnostics.current(
            errorText: errorText ?? viewModel.state.recordErrorText,
            languageDisplayName: languageDisplayName
        )
    }

    private func openAppSettings() {
        guard let settingsURL = URL(string: UIApplication.openSettingsURLString) else {
            return
        }
        openURL(settingsURL)
    }

    private func triggerErrorFeedback() {
        errorFeedbackTrigger += 1
        guard !reduceMotion else { return }
        withAnimation(.easeInOut(duration: 0.06)) {
            shakeOffset = -7
        }
        Task { @MainActor in
            try? await Task.sleep(for: .milliseconds(60))
            withAnimation(.easeInOut(duration: 0.06)) {
                shakeOffset = 7
            }
            try? await Task.sleep(for: .milliseconds(60))
            withAnimation(CozyTheme.Motion.snappySpring) {
                shakeOffset = 0
            }
        }
    }
}

#Preview("Voice To Text") {
    VoiceToTextScreen(
        onResult: { _ in },
        voiceToTextParser: IOSVoiceToTextParser(),
        languageCode: "en"
    )
}
