import Foundation
import Testing
import shared
@testable import iosApp

@MainActor
struct IOSVoiceToTextViewModelTests {

    @Test func initialStateDefaults() {
        let parser = FakeVoiceToTextParser()
        let viewModel = IOSVoiceToTextViewModel(
            voiceToTextParser: parser,
            languageCode: "en"
        )

        #expect(viewModel.state.powerRatios.isEmpty)
        #expect(viewModel.state.spokenText == "")
        #expect(viewModel.state.canRecord == false)
        #expect(viewModel.state.recordErrorText == nil)
        #expect(viewModel.state.displayState == nil)
    }

    @Test func startObservingGrantsPermissionAndEntersWaitingToTalk() async {
        let parser = FakeVoiceToTextParser()
        let viewModel = IOSVoiceToTextViewModel(
            voiceToTextParser: parser,
            languageCode: "en"
        )

        viewModel.startObserving()
        defer { viewModel.dispose() }

        let ready = await waitUntil {
            viewModel.state.canRecord && viewModel.state.displayState == .waitingToTalk
        }
        #expect(ready)
    }

    @Test func toggleRecordingStartsAndStopsParserAndUpdatesDisplayStates() async {
        let parser = FakeVoiceToTextParser()
        let viewModel = IOSVoiceToTextViewModel(
            voiceToTextParser: parser,
            languageCode: "en"
        )

        viewModel.startObserving()
        defer { viewModel.dispose() }

        #expect(await waitUntil { viewModel.state.displayState == .waitingToTalk })

        viewModel.onEvent(event: VoiceToTextEvent.ToggleRecording(languageCode: "en"))
        #expect(await waitUntil { parser.startListeningLanguageCodes == ["en"] })

        parser.emitState(result: "Hel", error: nil, powerRatio: 0.6, isSpeaking: true)
        let speaking = await waitUntil {
            viewModel.state.displayState == .speaking && !viewModel.state.powerRatios.isEmpty
        }
        #expect(speaking)
        #expect(viewModel.state.powerRatios.last?.floatValue == Float(0.6))

        viewModel.onEvent(event: VoiceToTextEvent.ToggleRecording(languageCode: "en"))
        #expect(await waitUntil { parser.stopListeningCallCount == 1 })

        parser.emitState(result: "Hello world", error: nil, powerRatio: 0.0, isSpeaking: false)
        let displayingResults = await waitUntil {
            viewModel.state.displayState == .displayingResults
                && viewModel.state.spokenText == "Hello world"
        }
        #expect(displayingResults)
    }

    @Test func parserErrorTransitionsDisplayStateToError() async {
        let parser = FakeVoiceToTextParser()
        let viewModel = IOSVoiceToTextViewModel(
            voiceToTextParser: parser,
            languageCode: "en"
        )

        viewModel.startObserving()
        defer { viewModel.dispose() }

        #expect(await waitUntil { viewModel.state.displayState == .waitingToTalk })

        parser.emitState(result: "", error: "Microphone unavailable", powerRatio: 0.0, isSpeaking: false)
        let errored = await waitUntil {
            viewModel.state.displayState == .error
                && viewModel.state.recordErrorText == "Microphone unavailable"
        }
        #expect(errored)
    }

    @Test func resetEventResetsParserAndClearsSpokenText() async {
        let parser = FakeVoiceToTextParser()
        let viewModel = IOSVoiceToTextViewModel(
            voiceToTextParser: parser,
            languageCode: "en"
        )

        viewModel.startObserving()
        defer { viewModel.dispose() }

        #expect(await waitUntil { viewModel.state.canRecord && viewModel.state.displayState == .waitingToTalk })

        parser.emitState(result: "Hello", error: nil, powerRatio: 0.0, isSpeaking: false)
        #expect(await waitUntil { viewModel.state.displayState == .displayingResults })

        viewModel.onEvent(event: VoiceToTextEvent.Reset())
        let resetDone = await waitUntil {
            parser.resetCallCount == 1
                && viewModel.state.spokenText == ""
                && viewModel.state.displayState == .error
        }
        #expect(resetDone)
        #expect(viewModel.state.canRecord == false)
    }

    @Test func disposeResetsParserAndStopsObservingFutureEmissions() async {
        let parser = FakeVoiceToTextParser()
        let viewModel = IOSVoiceToTextViewModel(
            voiceToTextParser: parser,
            languageCode: "en"
        )

        viewModel.startObserving()
        #expect(await waitUntil { viewModel.state.displayState == .waitingToTalk })

        viewModel.dispose()
        #expect(await waitUntil { parser.resetCallCount == 1 })

        let stateAtDispose = viewModel.state
        parser.emitState(result: "Should be ignored", error: nil, powerRatio: 0.5, isSpeaking: true)
        try? await Task.sleep(for: .milliseconds(100))

        #expect(viewModel.state.spokenText == stateAtDispose.spokenText)
        #expect(viewModel.state.displayState == stateAtDispose.displayState)
    }
}
