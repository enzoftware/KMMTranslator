//
//  TranslateTextField.swift
//  iosApp
//
//  Created by Enzo Lizama on 22/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import UniformTypeIdentifiers
import shared

struct TranslateTextField: View {

    @Binding var fromText: String
    let toText: String?
    let isTranslating: Bool
    let fromLanguage: UiLanguage
    let toLanguage: UiLanguage
    let onTranslateEvent: (TranslateEvent) -> Void

    var body: some View {
        if toText == nil || isTranslating {
            IdleTextField(
                fromText: $fromText,
                isTranslating: isTranslating,
                onTranslateEvent: onTranslateEvent
            )
            .gradientSurface()
            .cornerRadius(15)
            .animation(.easeInOut, value: isTranslating)
            .shadow(radius: 4)
        } else {
            TranslatedTextField(
                fromText: fromText,
                toText: toText ?? "",
                fromLanguage: fromLanguage,
                toLanguage: toLanguage,
                onTranslateEvent: onTranslateEvent
            )
            .padding()
            .gradientSurface()
            .cornerRadius(15)
            .animation(.easeInOut, value: isTranslating)
            .shadow(radius: 4)
            .onTapGesture {
                onTranslateEvent(.EditTranslation())
            }
        }
    }
}

#Preview {
    TranslateTextField(
        fromText: Binding(
            get: { "test" },
            set: { value in }
        ),
        toText: "Test",
        isTranslating: false,
        fromLanguage: UiLanguage(language: .english, imagePath: "english"),
        toLanguage: UiLanguage(language: .german, imagePath: "german"),
        onTranslateEvent: { event in

        }
    )
}

extension TranslateTextField {

    fileprivate struct IdleTextField: View {
        @Binding var fromText: String
        let isTranslating: Bool
        let onTranslateEvent: (TranslateEvent) -> Void

        var body: some View {
            TextEditor(text: $fromText)
                .frame(
                    maxWidth: .infinity,
                    minHeight: 200,
                    alignment: .topLeading
                )
                .padding()
                .foregroundColor(Color.onSurface)
                .overlay(alignment: .bottomTrailing) {
                    ProgressButton(
                        text: "Translate",
                        isLoading: isTranslating,
                        onClick: {
                            onTranslateEvent(TranslateEvent.Translate())
                        }
                    )
                    .padding(.trailing)
                    .padding(.bottom)
                }
                .onAppear {
                    UITextField.appearance().backgroundColor = .clear
                }
        }

    }
}

struct TranslatedTextField: View {
    let fromText: String
    let toText: String
    let fromLanguage: UiLanguage
    let toLanguage: UiLanguage
    let onTranslateEvent: (TranslateEvent) -> Void

    private let tts = TextToSpeech()

    var body: some View {
        VStack(alignment: .leading) {
            LanguageDisplay(language: fromLanguage)
            Text(fromText)
                .foregroundColor(.onSurface)

            HStack {
                Spacer()
                Button(
                    action: {
                        UIPasteboard.general.setValue(
                            fromText,
                            forPasteboardType: UTType.plainText.identifier
                        )
                    }
                ) {
                    Image(uiImage: UIImage(named: "copy")!)
                        .renderingMode(.template)
                        .foregroundColor(.lightBlue)

                }

                Button(
                    action: {
                        onTranslateEvent(.CloseTranslation())
                    }
                ) {
                    Image(systemName: "xmark")
                        .foregroundColor(.lightBlue)

                }
            }

            Divider().padding()

            LanguageDisplay(language: toLanguage)
                .padding(.bottom)

            Text(toText)
                .foregroundColor(.onSurface)

            HStack {
                Spacer()
                Button(
                    action: {
                        UIPasteboard.general.setValue(
                            toText,
                            forPasteboardType: UTType.plainText.identifier
                        )
                    }
                ) {
                    Image(uiImage: UIImage(named: "copy")!)
                        .renderingMode(.template)
                        .foregroundColor(.lightBlue)

                }

                Button(
                    action: {
                        tts.speak(
                            text: toText,
                            language: toLanguage.language.code
                        )
                    }
                ) {
                    Image(systemName: "speaker.wave.2")
                        .foregroundColor(.lightBlue)

                }
            }

        }
    }
}
