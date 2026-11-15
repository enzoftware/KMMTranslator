import Foundation
import Testing
import shared
@testable import iosApp

@MainActor
struct IOSPhrasebookViewModelTests {

    private let english = UiLanguage.companion.byCode(code: "en")
    private let german = UiLanguage.companion.byCode(code: "de")

    private func makeSUT() -> (IOSPhrasebookViewModel, FakeSavedWordsDataSource) {
        let dataSource = FakeSavedWordsDataSource()
        return (IOSPhrasebookViewModel(savedWordsDataSource: dataSource), dataSource)
    }

    @Test func startObservingShowsEveryPhraseForThePair() async {
        let (viewModel, _) = makeSUT()
        viewModel.startObserving(fromLanguage: english, toLanguage: german)
        defer { viewModel.dispose() }

        let loaded = await waitUntil { viewModel.state.suggestions.count == 25 && viewModel.state.toLanguage == german }
        #expect(loaded)
        let thanks = viewModel.state.suggestions.first { $0.entryKey == "thank_you" }
        #expect(thanks?.fromText == "Thank you")
        #expect(thanks?.toText == "Danke")
        #expect(thanks?.isSaved == false)
    }

    @Test func toggleSavedSavesThenRemovesTheWord() async {
        let (viewModel, dataSource) = makeSUT()
        viewModel.startObserving(fromLanguage: english, toLanguage: german)
        defer { viewModel.dispose() }
        _ = await waitUntil { viewModel.state.toLanguage == german }

        viewModel.onEvent(PhrasebookEvent.ToggleSaved(entryKey: "water"))
        let saved = await waitUntil { viewModel.state.savedWords.count == 1 }
        #expect(saved)
        #expect(dataSource.words.first?.toText == "Wasser")
        #expect(viewModel.state.suggestions.first { $0.entryKey == "water" }?.isSaved == true)

        viewModel.onEvent(PhrasebookEvent.ToggleSaved(entryKey: "water"))
        let removed = await waitUntil { viewModel.state.savedWords.isEmpty }
        #expect(removed)
        #expect(dataSource.words.isEmpty)
    }

    @Test func removeSavedWordDeletesIt() async {
        let (viewModel, dataSource) = makeSUT()
        viewModel.startObserving(fromLanguage: english, toLanguage: german)
        defer { viewModel.dispose() }
        _ = await waitUntil { viewModel.state.toLanguage == german }
        viewModel.onEvent(PhrasebookEvent.ToggleSaved(entryKey: "hello"))
        _ = await waitUntil { viewModel.state.savedWords.count == 1 }

        viewModel.onEvent(PhrasebookEvent.RemoveSavedWord(entryKey: "hello", fromLanguageCode: "en", toLanguageCode: "de"))

        let removed = await waitUntil { viewModel.state.savedWords.isEmpty }
        #expect(removed)
        #expect(dataSource.words.isEmpty)
    }

    @Test func categoryFilterNarrowsSuggestions() async {
        let (viewModel, _) = makeSUT()
        viewModel.startObserving(fromLanguage: english, toLanguage: german)
        defer { viewModel.dispose() }

        viewModel.onEvent(PhrasebookEvent.SelectCategory(category: .food))

        let filtered = await waitUntil {
            !viewModel.state.suggestions.isEmpty && viewModel.state.suggestions.allSatisfy { $0.category == .food }
        }
        #expect(filtered)
    }
}
