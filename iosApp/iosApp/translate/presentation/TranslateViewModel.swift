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
    @MainActor class TranslateViewModel: ObservableObject {
        private var historyDataSource: HistoryDataSource
        private var translateUseCase: TranslateUseCase
        private let viewModel : TranslateViewModel
        
        @Published var state: TranslateState = TranslateState(
            fromText: "",
            toText: nil,
            isTranslating: false,
            fromLanguage: UiLanguage(language: .english, imagePath: "english.png"),
            toLanguage: UiLanguage(language: .german, imagePath: "german"),
            isChoosingFromLanguage: false,
            isChoosingToLanguage: false ,
            error: nil,
            history: []
        )
        
        private var handle: DisposableHandle?
        
        init(historyDataSource: HistoryDataSource, translateUseCase: TranslateUseCase) {
            self.historyDataSource = historyDataSource
            self.translateUseCase = translateUseCase
            self.viewModel = TranslateViewModel(historyDataSource: historyDataSource, translateUseCase: translateUseCase)
        }
        
        func onEvent(event: TranslateEvent) {
                   self.viewModel.onEvent(event: event)
               }
               
               func startObserving() {
                   handle = viewModel.state.collec
               }
               
               func dispose() {
                   handle?.dispose()
               }
    }
}
