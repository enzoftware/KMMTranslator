import Foundation
import Testing
import shared
@testable import iosApp

@MainActor
struct IOSTranslateViewModelTests {

    private func makeSUT(
        client: FakeTranslateClient = FakeTranslateClient(),
        historyDataSource: FakeHistoryDataSource = FakeHistoryDataSource()
    ) -> (TranslateScreen.IOSTranslateViewModel, FakeTranslateClient, FakeHistoryDataSource) {
        let useCase = TranslateUseCase(
            client: client,
            historyDataSource: historyDataSource
        )
        let viewModel = TranslateScreen.IOSTranslateViewModel(
            historyDataSource: historyDataSource,
            translateUseCase: useCase
        )
        return (viewModel, client, historyDataSource)
    }

    @Test func initialStateDefaults() {
        let (viewModel, _, _) = makeSUT()

        #expect(viewModel.state.fromText == "")
        #expect(viewModel.state.toText == nil)
        #expect(viewModel.state.isTranslating == false)
        #expect(viewModel.state.fromLanguage.language == .english)
        #expect(viewModel.state.fromLanguage.imagePath == "english")
        #expect(viewModel.state.toLanguage.language == .german)
        #expect(viewModel.state.toLanguage.imagePath == "german")
        #expect(viewModel.state.isChoosingFromLanguage == false)
        #expect(viewModel.state.isChoosingToLanguage == false)
        #expect(viewModel.state.error == nil)
        #expect(viewModel.state.history.isEmpty)
    }

    @Test func startObservingSyncsExistingAndFutureHistory() async {
        let initialItem = HistoryItem(
            id: KotlinLong(value: 1),
            fromLanguageCode: "en",
            fromText: "Hello",
            toLanguageCode: "de",
            toText: "Hallo"
        )
        let historyDataSource = FakeHistoryDataSource(initialHistory: [initialItem])
        let (viewModel, _, _) = makeSUT(historyDataSource: historyDataSource)

        viewModel.startObserving()
        defer { viewModel.dispose() }

        let loadedInitial = await waitUntil {
            viewModel.state.history.count == 1
        }
        #expect(loadedInitial)
        #expect(viewModel.state.history.first?.fromText == "Hello")
        #expect(viewModel.state.history.first?.toText == "Hallo")

        let secondItem = HistoryItem(
            id: KotlinLong(value: 2),
            fromLanguageCode: "en",
            fromText: "Goodbye",
            toLanguageCode: "es",
            toText: "Adios"
        )
        historyDataSource.emitHistory([secondItem, initialItem])

        let loadedSecond = await waitUntil {
            viewModel.state.history.count == 2
        }
        #expect(loadedSecond)
        #expect(viewModel.state.history.first?.fromText == "Goodbye")
    }

    @Test func disposeStopsObservingStateUpdates() async {
        let (viewModel, _, _) = makeSUT()
        viewModel.startObserving()

        viewModel.onEvent(event: TranslateEvent.ChangeTranslationText(text: "Before dispose"))
        let updatedBeforeDispose = await waitUntil {
            viewModel.state.fromText == "Before dispose"
        }
        #expect(updatedBeforeDispose)

        viewModel.dispose()
        viewModel.onEvent(event: TranslateEvent.ChangeTranslationText(text: "After dispose"))
        try? await Task.sleep(for: .milliseconds(100))

        #expect(viewModel.state.fromText == "Before dispose")
    }

    @Test func callingStartObservingTwiceDoesNotLeakPreviousSubscription() async {
        let (viewModel, _, _) = makeSUT()
        viewModel.startObserving()
        viewModel.startObserving()
        defer { viewModel.dispose() }

        viewModel.onEvent(event: TranslateEvent.ChangeTranslationText(text: "Hello"))
        let updated = await waitUntil {
            viewModel.state.fromText == "Hello"
        }
        #expect(updated)

        viewModel.dispose()
        viewModel.onEvent(event: TranslateEvent.ChangeTranslationText(text: "Ignored"))
        try? await Task.sleep(for: .milliseconds(100))

        #expect(viewModel.state.fromText == "Hello")
    }

    @Test func chooseLanguagesAndDropdownEvents() async {
        let (viewModel, _, _) = makeSUT()
        viewModel.startObserving()
        defer { viewModel.dispose() }

        viewModel.onEvent(event: TranslateEvent.OpenFromLanguageDropDown())
        #expect(await waitUntil { viewModel.state.isChoosingFromLanguage })

        let spanish = UiLanguage.companion.byCode(code: "es")
        viewModel.onEvent(event: TranslateEvent.ChooseFromLanguage(language: spanish))
        #expect(await waitUntil {
            viewModel.state.fromLanguage.language == .spanish && !viewModel.state.isChoosingFromLanguage
        })

        viewModel.onEvent(event: TranslateEvent.OpenToLanguageDropDown())
        #expect(await waitUntil { viewModel.state.isChoosingToLanguage })

        viewModel.onEvent(event: TranslateEvent.StopChoosingLanguage())
        #expect(await waitUntil { !viewModel.state.isChoosingToLanguage })

        viewModel.onEvent(event: TranslateEvent.OpenToLanguageDropDown())
        #expect(await waitUntil { viewModel.state.isChoosingToLanguage })

        let french = UiLanguage.companion.byCode(code: "fr")
        viewModel.onEvent(event: TranslateEvent.ChooseToLanguage(language: french))
        #expect(await waitUntil {
            viewModel.state.toLanguage.language == .french && !viewModel.state.isChoosingToLanguage
        })
    }

    @Test func swapLanguagesExchangesFromAndToLanguages() async {
        let (viewModel, _, _) = makeSUT()
        viewModel.startObserving()
        defer { viewModel.dispose() }

        let synced = await waitUntil {
            viewModel.state.fromLanguage.language == .english
                && viewModel.state.toLanguage.language == .spanish
        }
        #expect(synced)

        let initialFrom = viewModel.state.fromLanguage.language
        let initialTo = viewModel.state.toLanguage.language

        viewModel.onEvent(event: TranslateEvent.SwapLanguages())
        let swapped = await waitUntil {
            viewModel.state.fromLanguage.language == initialTo
                && viewModel.state.toLanguage.language == initialFrom
        }
        #expect(swapped)
    }

    @Test func translateSuccessUpdatesToTextAndInsertsHistory() async {
        let client = FakeTranslateClient()
        client.result = .success("Hola")
        let historyDataSource = FakeHistoryDataSource()
        let (viewModel, _, _) = makeSUT(client: client, historyDataSource: historyDataSource)
        viewModel.startObserving()
        defer { viewModel.dispose() }

        let spanish = UiLanguage.companion.byCode(code: "es")
        viewModel.onEvent(event: TranslateEvent.ChooseToLanguage(language: spanish))
        viewModel.onEvent(event: TranslateEvent.ChangeTranslationText(text: "Hello"))
        viewModel.onEvent(event: TranslateEvent.Translate())

        let translated = await waitUntil {
            viewModel.state.toText == "Hola" && !viewModel.state.isTranslating
        }
        #expect(translated)
        #expect(client.calls.count == 1)
        #expect(client.calls.first?.fromLanguage == .english)
        #expect(client.calls.first?.fromText == "Hello")
        #expect(client.calls.first?.toLanguage == .spanish)
        #expect(historyDataSource.insertedItems.count == 1)
        #expect(historyDataSource.insertedItems.first?.fromText == "Hello")
        #expect(historyDataSource.insertedItems.first?.toText == "Hola")
    }

    @Test func translateWithBlankTextDoesNotInvokeClient() async {
        let client = FakeTranslateClient()
        let (viewModel, _, _) = makeSUT(client: client)
        viewModel.startObserving()
        defer { viewModel.dispose() }

        viewModel.onEvent(event: TranslateEvent.ChangeTranslationText(text: "   "))
        viewModel.onEvent(event: TranslateEvent.Translate())
        try? await Task.sleep(for: .milliseconds(100))

        #expect(client.calls.isEmpty)
        #expect(viewModel.state.toText == nil)
        #expect(viewModel.state.isTranslating == false)
    }

    @Test func translateFailureSetsErrorAndOnErrorClearsIt() async {
        let client = FakeTranslateClient()
        client.result = .failure(TranslateException(error: .serviceUnavailable).asError())
        let (viewModel, _, _) = makeSUT(client: client)
        viewModel.startObserving()
        defer { viewModel.dispose() }

        viewModel.onEvent(event: TranslateEvent.ChangeTranslationText(text: "Hello"))
        viewModel.onEvent(event: TranslateEvent.Translate())

        let errored = await waitUntil {
            viewModel.state.error != nil && !viewModel.state.isTranslating
        }
        #expect(errored)
        #expect(viewModel.state.error == .serviceUnavailable)

        viewModel.onEvent(event: TranslateEvent.OnError())
        let cleared = await waitUntil {
            viewModel.state.error == nil
        }
        #expect(cleared)
    }

    @Test func closeAndEditTranslationUpdateState() async {
        let client = FakeTranslateClient()
        client.result = .success("Hallo")
        let (viewModel, _, _) = makeSUT(client: client)
        viewModel.startObserving()
        defer { viewModel.dispose() }

        viewModel.onEvent(event: TranslateEvent.ChangeTranslationText(text: "Hello"))
        viewModel.onEvent(event: TranslateEvent.Translate())
        #expect(await waitUntil { viewModel.state.toText == "Hallo" })

        viewModel.onEvent(event: TranslateEvent.EditTranslation())
        #expect(await waitUntil {
            viewModel.state.toText == nil && viewModel.state.fromText == "Hello"
        })

        viewModel.onEvent(event: TranslateEvent.Translate())
        #expect(await waitUntil { viewModel.state.toText == "Hallo" })

        viewModel.onEvent(event: TranslateEvent.CloseTranslation())
        #expect(await waitUntil {
            viewModel.state.toText == nil && viewModel.state.fromText == ""
        })
    }

    @Test func selectHistoryItemPopulatesTranslationState() async {
        let (viewModel, _, _) = makeSUT()
        viewModel.startObserving()
        defer { viewModel.dispose() }

        let historyItem = UiHistoryItem(
            id: KotlinLong(value: 42),
            fromText: "Bonjour",
            toText: "Hello",
            fromLanguage: UiLanguage.companion.byCode(code: "fr"),
            toLanguage: UiLanguage.companion.byCode(code: "en")
        )
        viewModel.onEvent(event: TranslateEvent.SelectHistoryItem(item: historyItem))

        let selected = await waitUntil {
            viewModel.state.fromText == "Bonjour" && viewModel.state.toText == "Hello"
        }
        #expect(selected)
        #expect(viewModel.state.fromLanguage.language == .french)
        #expect(viewModel.state.toLanguage.language == .english)
    }

    @Test func submitVoiceResultUpdatesFromText() async {
        let client = FakeTranslateClient()
        client.result = .success("Hallo Welt")
        let (viewModel, _, _) = makeSUT(client: client)
        viewModel.startObserving()
        defer { viewModel.dispose() }

        viewModel.onEvent(event: TranslateEvent.SubmitVoiceResult(result: "Hello world"))

        let updated = await waitUntil {
            viewModel.state.fromText == "Hello world"
        }
        #expect(updated)
    }

    @Test func showPhrasebookEntryShowsTheOfflineTranslationInItsPair() async {
        let (viewModel, client, historyDataSource) = makeSUT()
        viewModel.startObserving()
        defer { viewModel.dispose() }

        viewModel.onEvent(
            event: TranslateEvent.ShowPhrasebookEntry(entryKey: "thank_you", fromLanguageCode: "fr", toLanguageCode: "ja")
        )

        let shown = await waitUntil { viewModel.state.toText == "ありがとうございます" }
        #expect(shown)
        #expect(viewModel.state.fromText == "Merci")
        #expect(viewModel.state.fromLanguage.language == .french)
        #expect(client.calls.isEmpty)
        #expect(historyDataSource.insertedItems.isEmpty)
    }
}
