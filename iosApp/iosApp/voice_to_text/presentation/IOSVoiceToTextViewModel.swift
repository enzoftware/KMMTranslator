//
//  IOSVoiceToTextViewModel.swift
//  iosApp
//
//  Created by Enzo Lizama on 24/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import Combine
import Foundation
import shared

@MainActor class IOSVoiceToTextViewModel: ObservableObject {
    private var voiceToTextParser: any VoiceToTextParser
    private let languageCode: String

    private let viewModel: VoiceToTextViewModel
    @Published var state = VoiceToTextState(
        powerRatios: [],
        spokenText: "",
        canRecord: false,
        recordErrorText: nil,
        displayState: nil
    )

    private var handle: (any Kotlinx_coroutines_coreDisposableHandle)?

    init(voiceToTextParser: any VoiceToTextParser, languageCode: String) {
        self.voiceToTextParser = voiceToTextParser
        self.languageCode = languageCode
        self.viewModel = VoiceToTextViewModel(
            voiceToTextParser: voiceToTextParser,
            coroutineScope: nil
        )
        self.viewModel.onEvent(
            event: VoiceToTextEvent.PermissionResult(
                isGranted: true,
                isPermanentDeclined: false
            )
        )
    }

    func onEvent(event: VoiceToTextEvent) {
        viewModel.onEvent(event: event)
    }

    func startObserving() {
        handle = viewModel.state.subscribe { [weak self] state in
            if let state {
                self?.state = state
            }
        }
    }

    func dispose() {
        handle?.dispose()
        onEvent(event: VoiceToTextEvent.Reset())
    }
}
