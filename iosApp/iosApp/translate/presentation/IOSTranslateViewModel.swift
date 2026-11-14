//
//  TranslateViewModel.swift
//  iosApp
//
//  Created by Enzo Lizama on 20/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import Foundation
import shared

extension TranslateScreen {
    @MainActor class IOSTranslateViewModel: ObservableObject {
        private var historyDataSource: HistoryDataSource
        private var translateUseCase: TranslateUseCase

        private let viewModel: TranslateViewModel

        @Published var state: TranslateState = TranslateState(
            fromText: "",
            toText: nil,
            isTranslating: false,
            fromLanguage: UiLanguage(
                language: .english,
                imagePath: "english"
            ),
            toLanguage: UiLanguage(language: .german, imagePath: "german"),
            isChoosingFromLanguage: false,
            isChoosingToLanguage: false,
            error: nil,
            history: []
        )

        private var handle: (any Kotlinx_coroutines_coreDisposableHandle)?

        init(
            historyDataSource: HistoryDataSource,
            translateUseCase: TranslateUseCase
        ) {
            self.historyDataSource = historyDataSource
            self.translateUseCase = translateUseCase
            self.viewModel = TranslateViewModel(
                translateUseCase: translateUseCase,
                historyDataSource: historyDataSource,
                coroutineScope: nil,
            )
        }

        func onEvent(event: TranslateEvent) {
            self.viewModel.onEvent(event: event)
        }

        func startObserving() {
            handle?.dispose()
            handle = viewModel.state.subscribe(onCollect: { [weak self] state in
                if let state = state {
                    self?.state = state
                }
            })
        }

        func dispose() {
            handle?.dispose()
            handle = nil
        }
    }
}
