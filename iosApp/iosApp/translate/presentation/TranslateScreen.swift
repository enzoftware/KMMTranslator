//
//  TranslateScreen.swift
//  iosApp
//
//  Created by Enzo Lizama on 20/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct TranslateScreen: View {
    private var historyDataSource: HistoryDataSource
    @StateObject private var voiceToTextParser = IOSVoiceToTextParser()
    private var translateUseCase: TranslateUseCase
    @ObservedObject var viewModel: IOSTranslateViewModel

    @Namespace private var voiceNamespace

    init(
        historyDataSource: HistoryDataSource,
        translateUseCase: TranslateUseCase
    ) {
        self.historyDataSource = historyDataSource
        self.translateUseCase = translateUseCase
        self.viewModel = IOSTranslateViewModel(
            historyDataSource: historyDataSource,
            translateUseCase: translateUseCase
        )
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                LazyVStack(alignment: .leading, spacing: CozyTheme.Spacing.lg) {
                    headerSection
                        .cozyEntrance(delay: 0.0)

                    languageBar
                        .cozyEntrance(delay: 0.05)

                    TranslateTextField(
                        fromText: Binding(
                            get: { viewModel.state.fromText },
                            set: { value in
                                viewModel.onEvent(
                                    event: .ChangeTranslationText(text: value)
                                )
                            }
                        ),
                        toText: viewModel.state.toText,
                        isTranslating: viewModel.state.isTranslating,
                        fromLanguage: viewModel.state.fromLanguage,
                        toLanguage: viewModel.state.toLanguage,
                        onTranslateEvent: { event in
                            withAnimation(CozyTheme.Motion.cozySpring) {
                                viewModel.onEvent(event: event)
                            }
                        }
                    )
                    .cozyEntrance(delay: 0.1)

                    historySection
                        .cozyEntrance(delay: 0.15)
                }
                .padding(.horizontal, CozyTheme.Spacing.md)
                .padding(.top, CozyTheme.Spacing.sm)
                .padding(.bottom, 104)
            }
            .scrollIndicators(.hidden)
            .scrollDismissesKeyboard(.interactively)

            floatingVoiceDock
                .padding(.bottom, CozyTheme.Spacing.md)
        }
        .overlay(alignment: .top) {
            if let error = viewModel.state.error {
                CozyBannerToast(message: errorMessage(for: error)) {
                    withAnimation(CozyTheme.Motion.snappySpring) {
                        viewModel.onEvent(event: .OnError())
                    }
                }
                .padding(.horizontal, CozyTheme.Spacing.md)
                .padding(.top, CozyTheme.Spacing.xs)
                .transition(.move(edge: .top).combined(with: .blurReplace))
            }
        }
        .animation(CozyTheme.Motion.cozySpring, value: viewModel.state.error != nil)
        .animation(CozyTheme.Motion.cozySpring, value: viewModel.state.history.count)
        .toolbar(.hidden, for: .navigationBar)
        .onAppear {
            viewModel.startObserving()
        }
        .onDisappear {
            viewModel.dispose()
        }
    }

    private var headerSection: some View {
        HStack(alignment: .center) {
            VStack(alignment: .leading, spacing: 3) {
                Text("Translator")
                    .font(.system(.largeTitle, design: .rounded, weight: .bold))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)

                Text("Warm notes across languages")
                    .font(.subheadline)
                    .foregroundStyle(CozyTheme.Colors.inkSecondary)
            }

            Spacer()

            HStack(spacing: 5) {
                Image(systemName: "character.book.closed.fill")
                    .font(.caption.weight(.semibold))
                Text("\(viewModel.state.history.count)")
                    .font(.caption.weight(.bold))
                    .contentTransition(.numericText())
            }
            .foregroundStyle(CozyTheme.Colors.indigo)
            .padding(.horizontal, 12)
            .padding(.vertical, 7)
            .background(
                Capsule()
                    .fill(CozyTheme.Colors.indigoSoft)
            )
        }
        .padding(.top, 4)
    }

    private var languageBar: some View {
        CozyGlassContainer(spacing: 10) {
            HStack(spacing: 10) {
                LanguageDropdown(
                    language: viewModel.state.fromLanguage,
                    isOpen: viewModel.state.isChoosingFromLanguage,
                    selectedLanguage: { language in
                        withAnimation(CozyTheme.Motion.snappySpring) {
                            viewModel.onEvent(
                                event: TranslateEvent.ChooseFromLanguage(language: language)
                            )
                        }
                    }
                )

                SwapLanguageButton {
                    withAnimation(CozyTheme.Motion.cozySpring) {
                        viewModel.onEvent(event: TranslateEvent.SwapLanguages())
                    }
                }

                LanguageDropdown(
                    language: viewModel.state.toLanguage,
                    isOpen: viewModel.state.isChoosingToLanguage,
                    selectedLanguage: { language in
                        withAnimation(CozyTheme.Motion.snappySpring) {
                            viewModel.onEvent(
                                event: TranslateEvent.ChooseToLanguage(language: language)
                            )
                        }
                    }
                )
            }
            .padding(8)
            .cozyFloatingChrome(in: Capsule(), interactive: true)
        }
    }

    @ViewBuilder
    private var historySection: some View {
        VStack(alignment: .leading, spacing: CozyTheme.Spacing.sm) {
            HStack {
                Text("Recent Translations")
                    .font(.system(.title3, design: .rounded, weight: .bold))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)

                Spacer()

                if !viewModel.state.history.isEmpty {
                    Text("\(viewModel.state.history.count) saved")
                        .font(.caption.weight(.medium))
                        .foregroundStyle(CozyTheme.Colors.inkMuted)
                        .contentTransition(.numericText())
                }
            }

            if viewModel.state.history.isEmpty {
                VStack(spacing: 10) {
                    Image(systemName: "text.bubble")
                        .font(.title2)
                        .foregroundStyle(CozyTheme.Colors.indigo.opacity(0.75))
                        .symbolEffect(.pulse.byLayer)

                    Text("No translations yet")
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)

                    Text("Your recent phrases will settle right here for quick replay.")
                        .font(.caption)
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                        .multilineTextAlignment(.center)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, CozyTheme.Spacing.lg)
                .padding(.horizontal, CozyTheme.Spacing.md)
                .cozyCard(cornerRadius: 20)
                .transition(.blurReplace)
            } else {
                LazyVStack(spacing: CozyTheme.Spacing.sm) {
                    ForEach(Array(viewModel.state.history.enumerated()), id: \.element.id) { index, item in
                        TranslateHistoryItem(item: item) {
                            withAnimation(CozyTheme.Motion.cozySpring) {
                                viewModel.onEvent(
                                    event: .SelectHistoryItem(item: item)
                                )
                            }
                        }
                        .cozyEntrance(delay: CozyTheme.Motion.staggeredDelay(for: index))
                        .scrollTransition(.interactive, axis: .vertical) { content, phase in
                            content
                                .opacity(phase.isIdentity ? 1.0 : 0.74)
                                .scaleEffect(phase.isIdentity ? 1.0 : 0.96)
                        }
                    }
                }
            }
        }
    }

    private var floatingVoiceDock: some View {
        CozyGlassContainer(spacing: 12) {
            NavigationLink {
                VoiceToTextScreen(
                    onResult: { spokenText in
                        viewModel.onEvent(
                            event: TranslateEvent.SubmitVoiceResult(result: spokenText)
                        )
                    },
                    voiceToTextParser: voiceToTextParser,
                    languageCode: viewModel.state.fromLanguage.language.code
                )
                .navigationTransition(.zoom(sourceID: "voice_recorder", in: voiceNamespace))
            } label: {
                HStack(spacing: 10) {
                    Image(systemName: "mic.fill")
                        .font(.system(size: 16, weight: .semibold))
                        .symbolEffect(.pulse.wholeSymbol, isActive: false)

                    Text("Voice Input")
                        .font(.subheadline.weight(.semibold))
                }
                .foregroundStyle(CozyTheme.Colors.onIndigo)
                .padding(.horizontal, 22)
                .padding(.vertical, 14)
                .cozyFloatingChrome(
                    in: Capsule(),
                    tint: CozyTheme.Colors.indigo,
                    interactive: true
                )
            }
            .buttonStyle(CozyScaleButtonStyle())
            .matchedTransitionSource(id: "voice_recorder", in: voiceNamespace)
            .accessibilityLabel("Open voice translator")
        }
    }

    private func errorMessage(for error: TranslateError) -> String {
        switch error {
        case .serviceUnavailable:
            return "Translation service is temporarily unavailable."
        case .clientError:
            return "Couldn't send that request. Please check your connection."
        case .serverError:
            return "The translation server ran into an issue."
        default:
            return "Something went wrong while translating."
        }
    }
}
