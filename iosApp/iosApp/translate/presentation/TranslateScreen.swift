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
    @EnvironmentObject private var settingsStore: SettingsStore

    @Namespace private var voiceNamespace
    @State private var isHistoryExpanded = false

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
                    personalGreetingBar
                        .cozyEntrance(delay: 0.0)

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
                    .cozyEntrance(delay: 0.06)

                    phrasebookSection
                        .cozyEntrance(delay: 0.12)
                }
                .padding(.horizontal, CozyTheme.Spacing.md)
                .padding(.top, CozyTheme.Spacing.sm)
                .padding(.bottom, 108)
            }
            .scrollIndicators(.hidden)
            .scrollDismissesKeyboard(.interactively)

            bottomCommandDock
                .padding(.horizontal, CozyTheme.Spacing.md)
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

    private var personalGreetingBar: some View {
        HStack(alignment: .center, spacing: 12) {
            HStack(spacing: 12) {
                ZStack {
                    Circle()
                        .fill(CozyTheme.Colors.indigoSoft)
                        .frame(width: 40, height: 40)
                        .overlay(
                            Circle()
                                .strokeBorder(CozyTheme.Colors.indigo.opacity(0.22), lineWidth: 1)
                        )

                    Text(settingsStore.initials)
                        .font(.system(.subheadline, design: .rounded, weight: .bold))
                        .foregroundStyle(CozyTheme.Colors.indigo)
                        .contentTransition(.numericText())
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(timeGreeting.uppercased())
                        .font(.system(size: 10, weight: .bold, design: .rounded))
                        .tracking(1.1)
                        .foregroundStyle(CozyTheme.Colors.inkMuted)

                    Text(
                        settingsStore.trimmedUserName.isEmpty
                            ? "Pocket Phrasebook"
                            : "Hi, \(settingsStore.trimmedUserName)"
                    )
                    .font(.system(.title3, design: .serif, weight: .semibold))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)
                    .contentTransition(.interpolate)
                }
            }

            Spacer()

            CozyGlassContainer(spacing: 8) {
                NavigationLink {
                    SettingsScreen()
                } label: {
                    Image(systemName: "gearshape.fill")
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                        .frame(width: 40, height: 40)
                        .cozyFloatingChrome(in: Circle(), interactive: true)
                }
                .buttonStyle(CozyScaleButtonStyle())
                .accessibilityLabel("Open settings")
            }
        }
        .padding(.top, 4)
    }

    @ViewBuilder
    private var phrasebookSection: some View {
        VStack(alignment: .leading, spacing: CozyTheme.Spacing.sm) {
            HStack(alignment: .center) {
                HStack(spacing: 8) {
                    Text("Saved Postcards")
                        .font(.system(.headline, design: .rounded, weight: .bold))
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)

                    if !viewModel.state.history.isEmpty {
                        Text("\(viewModel.state.history.count)")
                            .font(.caption2.weight(.bold))
                            .foregroundStyle(CozyTheme.Colors.indigo)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(
                                Capsule()
                                    .fill(CozyTheme.Colors.indigoSoft)
                            )
                            .contentTransition(.numericText())
                    }
                }

                Spacer()

                if viewModel.state.history.count > 1 {
                    Button {
                        withAnimation(CozyTheme.Motion.cozySpring) {
                            isHistoryExpanded.toggle()
                        }
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: isHistoryExpanded ? "rectangle.split.3x1" : "list.bullet")
                            Text(isHistoryExpanded ? "Shelf" : "See all")
                        }
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(CozyTheme.Colors.indigo)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(
                            Capsule()
                                .fill(CozyTheme.Colors.indigoSoft.opacity(0.75))
                        )
                    }
                    .buttonStyle(CozyScaleButtonStyle())
                }
            }

            if viewModel.state.history.isEmpty {
                HStack(spacing: 14) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .fill(CozyTheme.Colors.indigoSoft)
                            .frame(width: 46, height: 46)

                        Image(systemName: "book.pages.fill")
                            .font(.title3)
                            .foregroundStyle(CozyTheme.Colors.indigo)
                    }

                    VStack(alignment: .leading, spacing: 3) {
                        Text("Your shelf is waiting")
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(CozyTheme.Colors.inkPrimary)

                        Text("Translate a line above or tap a starter idiom to save your first postcard.")
                            .font(.caption)
                            .foregroundStyle(CozyTheme.Colors.inkSecondary)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(CozyTheme.Spacing.md)
                .cozyCard(cornerRadius: 22)
                .transition(.blurReplace)
            } else if isHistoryExpanded {
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
                    }
                }
                .transition(.blurReplace)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    LazyHStack(spacing: 12) {
                        ForEach(viewModel.state.history, id: \.id) { item in
                            TranslateHistoryItem(item: item, isPostcardShelf: true) {
                                withAnimation(CozyTheme.Motion.cozySpring) {
                                    viewModel.onEvent(
                                        event: .SelectHistoryItem(item: item)
                                    )
                                }
                            }
                            .scrollTransition(.interactive, axis: .horizontal) { content, phase in
                                content
                                    .scaleEffect(phase.isIdentity ? 1.0 : 0.95)
                                    .opacity(phase.isIdentity ? 1.0 : 0.85)
                            }
                        }
                    }
                    .scrollTargetLayout()
                    .padding(.vertical, 4)
                }
                .scrollTargetBehavior(.viewAligned)
                .contentMargins(.horizontal, CozyTheme.Spacing.md, for: .scrollContent)
                .padding(.horizontal, -CozyTheme.Spacing.md)
                .transition(.blurReplace)
            }
        }
    }

    private var bottomCommandDock: some View {
        CozyGlassContainer(spacing: 8) {
            HStack(spacing: 8) {
                HStack(spacing: 4) {
                    LanguageDropdown(
                        language: viewModel.state.fromLanguage,
                        isOpen: viewModel.state.isChoosingFromLanguage,
                        compact: true,
                        selectedLanguage: { language in
                            withAnimation(CozyTheme.Motion.snappySpring) {
                                viewModel.onEvent(
                                    event: TranslateEvent.ChooseFromLanguage(language: language)
                                )
                            }
                        }
                    )

                    SwapLanguageButton(size: 34) {
                        withAnimation(CozyTheme.Motion.cozySpring) {
                            viewModel.onEvent(event: TranslateEvent.SwapLanguages())
                        }
                    }

                    LanguageDropdown(
                        language: viewModel.state.toLanguage,
                        isOpen: viewModel.state.isChoosingToLanguage,
                        compact: true,
                        selectedLanguage: { language in
                            withAnimation(CozyTheme.Motion.snappySpring) {
                                viewModel.onEvent(
                                    event: TranslateEvent.ChooseToLanguage(language: language)
                                )
                            }
                        }
                    )
                }
                .padding(6)
                .cozyFloatingChrome(in: Capsule(), interactive: true)

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
                    Image(systemName: "mic.fill")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(CozyTheme.Colors.onIndigo)
                        .frame(width: 50, height: 50)
                        .cozyFloatingChrome(
                            in: Circle(),
                            tint: CozyTheme.Colors.indigo,
                            interactive: true
                        )
                }
                .buttonStyle(CozyScaleButtonStyle())
                .matchedTransitionSource(id: "voice_recorder", in: voiceNamespace)
                .accessibilityLabel("Open voice translator")
            }
        }
    }

    private var timeGreeting: String {
        let hour = Calendar.current.component(.hour, from: Date())
        switch hour {
        case 5..<12:
            return "Good morning"
        case 12..<17:
            return "Good afternoon"
        case 17..<22:
            return "Good evening"
        default:
            return "Late night notes"
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
