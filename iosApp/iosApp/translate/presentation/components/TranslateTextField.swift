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
        .onDisappear {
            tts.stop()
        }
    }
}

extension TranslateTextField {
    fileprivate struct IdleTextField: View {
        @Binding var fromText: String
        let fromLanguage: UiLanguage
        let isTranslating: Bool
        @FocusState.Binding var isEditorFocused: Bool
        let onTranslateEvent: (TranslateEvent) -> Void

        private var isBlank: Bool {
            fromText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
        }

        var body: some View {
            VStack(alignment: .leading, spacing: CozyTheme.Spacing.sm) {
                HStack {
                    LanguageDisplay(language: fromLanguage)

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
                        Text("Write or paste something to translate...")
                            .font(.body)
                            .foregroundStyle(CozyTheme.Colors.inkMuted)
                            .padding(.top, 8)
                            .padding(.leading, 5)
                            .allowsHitTesting(false)
                    }

                    TextEditor(text: $fromText)
                        .focused($isEditorFocused)
                        .font(.body)
                        .foregroundStyle(CozyTheme.Colors.inkPrimary)
                        .scrollContentBackground(.hidden)
                        .frame(minHeight: 148, maxHeight: 220, alignment: .topLeading)
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
                        .font(.body)
                        .foregroundStyle(CozyTheme.Colors.inkSecondary)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Label("Tap to edit", systemImage: "pencil")
                        .font(.caption2.weight(.medium))
                        .foregroundStyle(CozyTheme.Colors.inkMuted)
                }
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

                Image(systemName: "arrow.down.circle.fill")
                    .font(.caption)
                    .foregroundStyle(CozyTheme.Colors.indigo.opacity(0.6))

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
                    .font(.title3.weight(.medium))
                    .foregroundStyle(CozyTheme.Colors.inkPrimary)
                    .textSelection(.enabled)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .contentTransition(.interpolate)
                    .padding(.top, 2)
            }
        }
    }
}

#Preview {
    VStack {
        TranslateTextField(
            fromText: .constant("Hello, how are you today?"),
            toText: "Hola, ¿cómo estás hoy?",
            isTranslating: false,
            fromLanguage: UiLanguage(language: .english, imagePath: "english"),
            toLanguage: UiLanguage(language: .spanish, imagePath: "spanish"),
            onTranslateEvent: { _ in }
        )
        .padding()
    }
    .background(CozyAmbientBackground())
}
