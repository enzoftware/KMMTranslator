//
//  TranslateTextField.swift
//  iosApp
//
//  Created by Enzo Lizama on 22/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct TranslateTextField: View {
    @Binding var fromText: String
    let toText: String?
    let isTranslating: Bool
    let fromLanguage: UiLanguage
    let toLanguage: UiLanguage
    let onTranslateEvent: (TranslateEvent) -> Void

    @FocusState private var isEditorFocused: Bool
    @StateObject private var tts = TextToSpeech()

    private var isShowingResult: Bool {
        toText != nil && !isTranslating
    }

    var body: some View {
        Group {
            if !isShowingResult {
                IdleTextField(
                    fromText: $fromText,
                    fromLanguage: fromLanguage,
                    toLanguage: toLanguage,
                    isTranslating: isTranslating,
                    isEditorFocused: $isEditorFocused,
                    onTranslateEvent: onTranslateEvent
                )
                .transition(.blurReplace)
            } else {
                TranslatedTextField(
                    fromText: fromText,
                    toText: toText ?? "",
                    fromLanguage: fromLanguage,
                    toLanguage: toLanguage,
                    tts: tts,
                    onTranslateEvent: onTranslateEvent
                )
                .transition(.blurReplace)
            }
        }
        .padding(CozyTheme.Spacing.md)
        .cozyCard(cornerRadius: CozyTheme.Radius.card, isElevated: true)
        .animation(CozyTheme.Motion.cozySpring, value: isShowingResult)
        .animation(CozyTheme.Motion.snappySpring, value: isTranslating)
        .onChange(of: toText) { _, _ in
            tts.stop()
        }
        .onChange(of: toLanguage.language.code) { _, _ in
            tts.stop()
        }
        .onDisappear {
            tts.stop()
        }
    }
}

extension TranslateTextField {
    fileprivate struct IdleTextField: View {
        @Binding var fromText: String
        let fromLanguage: UiLanguage
        let toLanguage: UiLanguage
        let isTranslating: Bool
        @FocusState.Binding var isEditorFocused: Bool
        let onTranslateEvent: (TranslateEvent) -> Void

        private let starterIdioms = [
            "Break a leg",
            "Spill the tea",
            "Piece of cake",
            "Under the weather"
        ]

        private var isBlank: Bool {
            fromText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
        }

        var body: some View {
            VStack(alignment: .leading, spacing: CozyTheme.Spacing.sm) {
                HStack(spacing: 8) {
                    LanguageDisplay(language: fromLanguage)

                    Image(systemName: "arrow.right")
                        .font(.caption2.weight(.bold))
                        .foregroundStyle(CozyTheme.Colors.inkMuted)

                    Text(toLanguage.language.languageName)
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(CozyTheme.Colors.indigo)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(
                            Capsule()
                                .fill(CozyTheme.Colors.indigoSoft)
                        )

                    Spacer()

                    if !fromText.isEmpty && !isTranslating {
                        Button {
                            withAnimation(CozyTheme.Motion.snappySpring) {
                                onTranslateEvent(.CloseTranslation())
                            }
                        } label: {
                            HStack(spacing: 4) {
                                Image(systemName: "xmark.circle.fill")
                                Text("Clear")
                            }
                            .font(.caption.weight(.medium))
                            .foregroundStyle(CozyTheme.Colors.inkSecondary)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 5)
                            .background(
                                Capsule()
                                    .fill(CozyTheme.Colors.paperMuted.opacity(0.75))
                            )
                        }
                        .buttonStyle(CozyScaleButtonStyle())
                        .transition(.blurReplace)
                    }
                }

                ZStack(alignment: .topLeading) {
                    if fromText.isEmpty {
                        Text("Write a thought, quote, or idiom…")
                            .font(.system(.body, design: .serif))
                            .foregroundStyle(CozyTheme.Colors.inkMuted)
                            .padding(.top, 10)
                            .padding(.leading, 8)
                            .allowsHitTesting(false)
                    }

                    TextEditor(text: $fromText)
                        .focused($isEditorFocused)
                        .font(.body)
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                        .scrollContentBackground(.hidden)
                        .padding(4)
                        .frame(minHeight: 132, maxHeight: 200, alignment: .topLeading)
                        .disabled(isTranslating)
                        .toolbar {
                            ToolbarItemGroup(placement: .keyboard) {
                                Spacer()
                                Button("Done") {
                                    isEditorFocused = false
                                }
                                .font(.subheadline.weight(.semibold))
                                .foregroundStyle(CozyTheme.Colors.indigo)
                            }
                        }
                }
                .background(
                    RoundedRectangle(cornerRadius: CozyTheme.Radius.inner, style: .continuous)
                        .fill(CozyTheme.Colors.paperMuted.opacity(0.32))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: CozyTheme.Radius.inner, style: .continuous)
                        .strokeBorder(
                            isEditorFocused ? CozyTheme.Colors.indigo.opacity(0.45) : CozyTheme.Colors.borderSubtle.opacity(0.7),
                            lineWidth: 1
                        )
                )

                if fromText.isEmpty && fromLanguage.language == .english {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(starterIdioms, id: \.self) { phrase in
                                Button {
                                    withAnimation(CozyTheme.Motion.snappySpring) {
                                        onTranslateEvent(.ChangeTranslationText(text: phrase))
                                    }
                                } label: {
                                    HStack(spacing: 5) {
                                        Image(systemName: "quote.opening")
                                            .font(.system(size: 9, weight: .bold))
                                            .foregroundStyle(CozyTheme.Colors.indigo)

                                        Text(phrase)
                                            .font(.caption.weight(.medium))
                                            .foregroundStyle(CozyTheme.Colors.inkSecondary)
                                    }
                                    .padding(.horizontal, 11)
                                    .padding(.vertical, 7)
                                    .background(
                                        Capsule()
                                            .fill(CozyTheme.Colors.paperMuted.opacity(0.65))
                                    )
                                    .overlay(
                                        Capsule()
                                            .strokeBorder(CozyTheme.Colors.borderSubtle, lineWidth: 1)
                                    )
                                }
                                .buttonStyle(CozyScaleButtonStyle())
                            }
                        }
                    }
                    .transition(.blurReplace)
                }

                HStack(alignment: .center) {
                    Text("\(fromText.count) chars")
                        .font(.caption.weight(.medium))
                        .foregroundStyle(CozyTheme.Colors.inkMuted)
                        .contentTransition(.numericText())
                        .animation(CozyTheme.Motion.snappySpring, value: fromText.count)

                    Spacer()

                    ProgressButton(
                        text: "Translate",
                        isLoading: isTranslating,
                        isDisabled: isBlank,
                        onClick: {
                            isEditorFocused = false
                            onTranslateEvent(TranslateEvent.Translate())
                        }
                    )
                }
                .padding(.top, 4)
            }
        }
    }
}

struct TranslatedTextField: View {
    let fromText: String
    let toText: String
    let fromLanguage: UiLanguage
    let toLanguage: UiLanguage
    @ObservedObject var tts: TextToSpeech
    let onTranslateEvent: (TranslateEvent) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: CozyTheme.Spacing.md) {
            VStack(alignment: .leading, spacing: CozyTheme.Spacing.sm) {
                HStack(spacing: 8) {
                    LanguageDisplay(language: fromLanguage)

                    Spacer()

                    CozyCopyChip(text: fromText)

                    Button {
                        tts.stop()
                        withAnimation(CozyTheme.Motion.snappySpring) {
                            onTranslateEvent(.CloseTranslation())
                        }
                    } label: {
                        Image(systemName: "xmark")
                            .font(.caption.weight(.bold))
                            .foregroundStyle(CozyTheme.Colors.inkSecondary)
                            .padding(8)
                            .background(Circle().fill(CozyTheme.Colors.paperMuted.opacity(0.85)))
                    }
                    .buttonStyle(CozyScaleButtonStyle())
                    .accessibilityLabel("Close translation")
                }

                VStack(alignment: .leading, spacing: 6) {
                    Text(fromText)
                        .font(.subheadline)
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Label("Tap to edit", systemImage: "pencil")
                        .font(.caption2.weight(.medium))
                        .foregroundStyle(CozyTheme.Colors.inkMuted)
                }
                .padding(12)
                .background(
                    RoundedRectangle(cornerRadius: CozyTheme.Radius.inner, style: .continuous)
                        .fill(CozyTheme.Colors.paperMuted.opacity(0.35))
                )
                .contentShape(Rectangle())
                .onTapGesture {
                    tts.stop()
                    withAnimation(CozyTheme.Motion.cozySpring) {
                        onTranslateEvent(.EditTranslation())
                    }
                }
            }

            HStack(spacing: 10) {
                Rectangle()
                    .fill(CozyTheme.Colors.borderSubtle)
                    .frame(height: 1)

                Image(systemName: "sparkles")
                    .font(.caption2.weight(.semibold))
                    .foregroundStyle(CozyTheme.Colors.indigo.opacity(0.75))

                Rectangle()
                    .fill(CozyTheme.Colors.borderSubtle)
                    .frame(height: 1)
            }

            VStack(alignment: .leading, spacing: CozyTheme.Spacing.sm) {
                HStack(spacing: 8) {
                    LanguageDisplay(language: toLanguage)

                    Spacer()

                    CozyCopyChip(text: toText)

                    CozySpeakButton(isSpeaking: tts.isSpeaking) {
                        tts.speak(
                            text: toText,
                            language: toLanguage.language.code
                        )
                    }
                }

                Text(toText)
                    .font(.system(.title2, design: .serif, weight: .medium))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)
                    .textSelection(.enabled)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .contentTransition(.interpolate)
                    .padding(.top, 4)
            }
        }
    }
}

#Preview("Idle & Translated") {
    VStack(spacing: 16) {
        TranslateTextField(
            fromText: .constant(""),
            toText: nil,
            isTranslating: false,
            fromLanguage: UiLanguage(language: .english, imagePath: "english"),
            toLanguage: UiLanguage(language: .spanish, imagePath: "spanish"),
            onTranslateEvent: { _ in }
        )

        TranslateTextField(
            fromText: .constant("Break a leg tonight!"),
            toText: "¡Mucha suerte esta noche!",
            isTranslating: false,
            fromLanguage: UiLanguage(language: .english, imagePath: "english"),
            toLanguage: UiLanguage(language: .spanish, imagePath: "spanish"),
            onTranslateEvent: { _ in }
        )
    }
    .padding()
    .background(CozyAmbientBackground())
}
