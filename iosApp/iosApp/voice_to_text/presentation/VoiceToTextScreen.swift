//
//  VoiceToTextScreen.swift
//  iosApp
//
//  Created by Enzo Lizama on 24/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct VoiceToTextScreen: View {
    private let onResult: (String) -> Void

    @ObservedObject var viewModel: IOSVoiceToTextViewModel
    private let voiceToTextParser: any VoiceToTextParser
    private let languageCode: String

    @Environment(\.dismiss) private var dismiss

    init(
        onResult: @escaping (String) -> Void,
        voiceToTextParser: any VoiceToTextParser,
        languageCode: String
    ) {
        self.onResult = onResult
        self.voiceToTextParser = voiceToTextParser
        self.languageCode = languageCode
        self.viewModel = IOSVoiceToTextViewModel(
            voiceToTextParser: voiceToTextParser,
            languageCode: languageCode
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

                Spacer()

                mainStage
                    .id(currentDisplayState)
                    .transition(.blurReplace)

                Spacer()

                bottomDock
            }
            .padding(.horizontal, CozyTheme.Spacing.md)
            .padding(.top, CozyTheme.Spacing.sm)
            .padding(.bottom, CozyTheme.Spacing.lg)
        }
        .animation(CozyTheme.Motion.cozySpring, value: currentDisplayState)
        .toolbar(.hidden, for: .navigationBar)
        .onAppear {
            viewModel.startObserving()
        }
        .onDisappear {
            viewModel.dispose()
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
                        .cozyFloatingChrome(in: Circle(), interactive: true)
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
                        .fill(CozyTheme.Colors.indigoSoft)
                        .frame(width: 76, height: 76)

                    Image(systemName: "waveform.circle.fill")
                        .font(.system(size: 38))
                        .foregroundStyle(CozyTheme.Colors.indigo)
                        .symbolEffect(.pulse.byLayer)
                }

                VStack(spacing: 6) {
                    Text("Ready when you are")
                        .font(.system(.title2, design: .rounded, weight: .bold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)

                    Text("Tap the microphone below and start speaking in \(languageDisplayName).")
                        .font(.subheadline)
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                        .multilineTextAlignment(.center)
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

                    Image(systemName: "mic.slash.fill")
                        .font(.title2.weight(.semibold))
                        .foregroundStyle(CozyTheme.Colors.danger)
                }

                Text("Couldn't capture audio")
                    .font(.system(.title3, design: .rounded, weight: .bold))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)

                Text(viewModel.state.recordErrorText ?? "Unknown error")
                    .font(.subheadline)
                    .foregroundStyle(CozyTheme.Colors.danger)
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity)
            .padding(CozyTheme.Spacing.xl)
            .cozyCard(cornerRadius: CozyTheme.Radius.card, isElevated: true)

        default:
            EmptyView()
        }
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
                    onClick: {
                        if currentDisplayState != .displayingResults {
                            viewModel.onEvent(
                                event: VoiceToTextEvent.ToggleRecording(
                                    languageCode: languageCode
                                )
                            )
                        } else {
                            onResult(viewModel.state.spokenText)
                            dismiss()
                        }
                    }
                )

                if currentDisplayState == .displayingResults || currentDisplayState == .error {
                    Button {
                        viewModel.onEvent(
                            event: VoiceToTextEvent.ToggleRecording(
                                languageCode: languageCode
                            )
                        )
                    } label: {
                        Image(systemName: "arrow.clockwise")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(CozyTheme.Colors.inkPrimary)
                            .frame(width: 52, height: 52)
                            .cozyFloatingChrome(in: Circle(), interactive: true)
                    }
                    .buttonStyle(CozyScaleButtonStyle())
                    .transition(.scale.combined(with: .opacity))
                    .accessibilityLabel("Record again")
                }
            }
        }
    }
}
