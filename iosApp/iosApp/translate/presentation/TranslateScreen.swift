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
    private var translateUseCase: TranslateUseCase
    @ObservedObject var viewModel: IOSTranslateViewModel
    `@StateObject` private var voiceToTextParser = IOSVoiceToTextParser()

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
        ZStack {
            List {
                HStack(alignment: .center) {
                    LanguageDropdown(
                        language: viewModel.state.fromLanguage,
                        isOpen: viewModel.state.isChoosingFromLanguage,
                        selectedLanguage: { language in
                            viewModel.onEvent(
                                event: TranslateEvent.ChooseFromLanguage(
                                    language: language
                                )
                            )
                        }
                    )
                    Spacer()
                    SwapLanguageButton(
                        onClick: {
                            viewModel.onEvent(
                                event: TranslateEvent.SwapLanguages()
                            )
                        }
                    )
                    Spacer()
                    LanguageDropdown(
                        language: viewModel.state.toLanguage,
                        isOpen: viewModel.state.isChoosingToLanguage,
                        selectedLanguage: { language in
                            viewModel.onEvent(
                                event: TranslateEvent.ChooseToLanguage(
                                    language: language
                                )
                            )
                        }
                    )
                }
                .listRowSeparator(.hidden)
                .listRowBackground(Color.background)

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
                        viewModel.onEvent(event: event)
                    }
                )
                .listRowSeparator(.hidden)
                .listRowBackground(Color.background)

                if !viewModel.state.history.isEmpty {
                    Text("History")
                        .font(.title)
                        .bold()
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .listRowSeparator(.hidden)
                        .listRowBackground(Color.background)
                }

                ForEach(viewModel.state.history, id: \.self.id) { item in
                    TranslateHistoryItem(
                        item: item,
                        onClick: {
                            viewModel.onEvent(
                                event: .SelectHistoryItem(item: item)
                            )
                        }
                    )
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.background)
                }

            }
            .listStyle(.plain)
            .buttonStyle(.plain)

            VStack {
                Spacer()
                NavigationLink(
                    destination: VoiceToTextScreen(
                        onResult: {
                            spokenText in
                            viewModel.onEvent(
                                event: TranslateEvent.SubmitVoiceResult(
                                    result: spokenText
                                )
                            )
                        },
                        voiceToTextParser: voiceToTextParser,
                        languageCode: viewModel.state.fromLanguage.language.code
                    )
                ) {
                    ZStack {
                        Circle()
                            .foregroundColor(.primaryColor)
                            .padding()
                        Image(uiImage: UIImage(named: "mic")!)
                            .foregroundColor(.onPrimary)
                    }
                    .frame(maxWidth: 100, maxHeight: 100)
                }
            }
        }
        .onAppear {
            viewModel.startObserving()
        }
        .onDisappear {
            viewModel.dispose()
        }
    }
}
