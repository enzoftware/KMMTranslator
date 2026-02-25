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

    @Environment(\.presentationMode) var presentation

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

    var body: some View {
        VStack {
            Spacer()
            mainView
            Spacer()
            HStack {
                Spacer()
                VoiceRecorderButton(
                    displayState: viewModel.state.displayState
                        ?? .waitingToTalk,
                    onClick: {
                        if viewModel.state.displayState != .displayingResults {
                            viewModel.onEvent(
                                event: VoiceToTextEvent.ToggleRecording(
                                    languageCode: languageCode
                                )
                            )
                        } else {
                            onResult(viewModel.state.spokenText)
                            self.presentation.wrappedValue.dismiss()
                        }

                    }
                )
                if viewModel.state.displayState == .displayingResults {
                    Button(action: {
                        viewModel.onEvent(
                            event: VoiceToTextEvent.ToggleRecording(
                                languageCode: languageCode
                            )
                        )
                    }) {
                        Image(systemName: "arrow.clockwise")
                            .foregroundColor(.lightBlue)
                    }
                }
                Spacer()
            }
        }
        .onAppear {
            viewModel.startObserving()
        }
        .onDisappear {
            viewModel.dispose()
        }
        .background(Color.background)
    }

    var mainView: some View {
        if let displayState = viewModel.state.displayState {
            switch displayState {
            case .waitingToTalk:
                return AnyView(
                    Text("Click record and start talking")
                        .font(.title2)
                )
            case .displayingResults:
                return AnyView(
                    Text(viewModel.state.spokenText)
                        .font(.title2)
                )

            case .error:
                return AnyView(
                    Text(viewModel.state.recordErrorText ?? "Unknown error")
                        .font(.title2)
                        .foregroundColor(.red)
                )
            case .speaking:
                return AnyView(
                    VoiceRecorderDisplay(
                        powerRatios: viewModel.state.powerRatios.map { ratio in
                            Double.init(truncating: ratio)
                        }
                    )
                    .frame(maxHeight: 100.0)
                    .padding()
                )
            default:
                return AnyView(EmptyView())
            }
        } else {
            return AnyView(EmptyView())
        }
    }
}
