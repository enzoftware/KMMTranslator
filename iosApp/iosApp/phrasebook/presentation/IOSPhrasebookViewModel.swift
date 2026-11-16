//
//  IOSPhrasebookViewModel.swift
//  iosApp
//

import Foundation
import shared

/// Bridges the shared `PhrasebookViewModel` into SwiftUI.
@MainActor
final class IOSPhrasebookViewModel: ObservableObject {
    private let viewModel: PhrasebookViewModel
    private var handle: (any Kotlinx_coroutines_coreDisposableHandle)?

    @Published private(set) var state = PhrasebookState(
        fromLanguage: UiLanguage.companion.byCode(code: "en"),
        toLanguage: UiLanguage.companion.byCode(code: "es"),
        selectedCategory: nil,
        suggestions: [],
        savedWords: []
    )

    init(savedWordsDataSource: SavedWordsDataSource) {
        self.viewModel = PhrasebookViewModel(
            savedWordsDataSource: savedWordsDataSource,
            coroutineScope: nil,
            now: { KotlinLong(value: Int64(Date().timeIntervalSince1970 * 1000)) }
        )
    }

    deinit {
        // The shared view model owns its coroutine scope on iOS; cancel it with this wrapper.
        viewModel.dispose()
    }

    func onEvent(_ event: PhrasebookEvent) {
        viewModel.onEvent(event: event)
    }

    func startObserving(fromLanguage: UiLanguage, toLanguage: UiLanguage) {
        handle?.dispose()
        onEvent(PhrasebookEvent.SetLanguages(fromLanguage: fromLanguage, toLanguage: toLanguage))
        handle = viewModel.state.subscribe(onCollect: { [weak self] state in
            if let state {
                self?.state = state
            }
        })
    }

    func dispose() {
        handle?.dispose()
        handle = nil
    }
}
