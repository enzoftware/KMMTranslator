import AVFoundation
import Combine
import Foundation
import Speech
import Testing
import shared
@testable import iosApp

@MainActor
struct IOSVoiceToTextParserTests {

    private func makeParser(
        micObserver: MockMicrophonePowerObserver = MockMicrophonePowerObserver(),
        audioSession: MockAudioSession = MockAudioSession(),
        supportedLocales: Set<Locale> = [Locale(identifier: "en-US"), Locale(identifier: "es-ES")],
        recognizer: MockSpeechRecognizer? = MockSpeechRecognizer(),
        speechAuthStatus: SFSpeechRecognizerAuthorizationStatus = .authorized,
        audioEngine: MockAudioEngine = MockAudioEngine(),
        capturedLocale: ((Locale) -> Void)? = nil
    ) -> IOSVoiceToTextParser {
        IOSVoiceToTextParser(
            micObserver: micObserver,
            audioSession: audioSession,
            supportedLocalesProvider: { supportedLocales },
            recognizerFactory: { locale in
                capturedLocale?(locale)
                return recognizer
            },
            speechAuthorizationRequester: { completion in
                completion(speechAuthStatus)
            },
            audioEngineFactory: { audioEngine }
        )
    }

    @Test func initialStateDefaults() {
        let parser = makeParser()
        let state = parser.state.value

        #expect(state?.result == "")
        #expect(state?.error == nil)
        #expect(state?.powerRatio == 0.0)
        #expect(state?.isSpeaking == false)
    }

    @Test func startListeningSelectsSupportedLocaleOrFallsBackToEnUS() {
        var requestedLocales: [Locale] = []
        let parser = makeParser(
            supportedLocales: [Locale(identifier: "en-US"), Locale(identifier: "es-ES")],
            capturedLocale: { requestedLocales.append($0) }
        )

        parser.startListening(languageCode: "es-ES")
        parser.stopListening()
        parser.startListening(languageCode: "xx-XX")
        parser.stopListening()

        #expect(requestedLocales == [Locale(identifier: "es-ES"), Locale(identifier: "en-US")])
    }

    @Test func startListeningFailsWhenRecognizerUnavailable() {
        let recognizer = MockSpeechRecognizer(isAvailable: false)
        let audioSession = MockAudioSession()
        let parser = makeParser(audioSession: audioSession, recognizer: recognizer)

        parser.startListening(languageCode: "en-US")

        #expect(parser.state.value?.error == "Speech recognizer is not available")
        #expect(parser.state.value?.isSpeaking == false)
        #expect(audioSession.requestRecordPermissionCallCount == 0)
    }

    @Test func startListeningFailsWhenRecognizerIsNil() {
        let parser = makeParser(recognizer: nil)

        parser.startListening(languageCode: "en-US")

        #expect(parser.state.value?.error == "Speech recognizer is not available")
        #expect(parser.state.value?.isSpeaking == false)
    }

    @Test func startListeningFailsWhenRecordPermissionDenied() {
        let audioSession = MockAudioSession()
        audioSession.recordPermissionGranted = false
        let parser = makeParser(audioSession: audioSession)

        parser.startListening(languageCode: "en-US")

        #expect(parser.state.value?.error == "You need to grant permission to record your voice.")
        #expect(parser.state.value?.isSpeaking == false)
    }

    @Test func startListeningFailsWhenSpeechAuthorizationDenied() {
        let parser = makeParser(speechAuthStatus: .denied)

        parser.startListening(languageCode: "en-US")

        #expect(parser.state.value?.error == "You need to grant permission to transcribe audio.")
        #expect(parser.state.value?.isSpeaking == false)
    }

    @Test func startListeningFailsOnInvalidAudioFormatMetrics() {
        let inputNode = MockAudioInputNode(
            formatMetrics: AudioFormatMetrics(sampleRate: 0, channelCount: 0, isOtherFormat: false)
        )
        let engine = MockAudioEngine(inputNode: inputNode)
        let parser = makeParser(audioEngine: engine)

        parser.startListening(languageCode: "en-US")

        #expect(
            parser.state.value?.error
                == "Invalid audio format from input node - please test on a physical device"
        )
        #expect(parser.state.value?.isSpeaking == false)
        #expect(inputNode.installTapCallCount == 0)
    }

    @Test func startListeningFailsOnUnsupportedOtherAudioFormat() {
        let inputNode = MockAudioInputNode(
            formatMetrics: AudioFormatMetrics(sampleRate: 44100, channelCount: 1, isOtherFormat: true)
        )
        let engine = MockAudioEngine(inputNode: inputNode)
        let parser = makeParser(audioEngine: engine)

        parser.startListening(languageCode: "en-US")

        #expect(
            parser.state.value?.error
                == "Unsupported audio format - please test on a physical device"
        )
        #expect(parser.state.value?.isSpeaking == false)
        #expect(inputNode.installTapCallCount == 0)
    }

    @Test func startListeningHandlesAudioEngineStartError() {
        struct AudioStartFailure: LocalizedError {
            var errorDescription: String? { "Engine failed to start" }
        }
        let engine = MockAudioEngine()
        engine.startError = AudioStartFailure()
        let parser = makeParser(audioEngine: engine)

        parser.startListening(languageCode: "en-US")

        #expect(parser.state.value?.error == "Engine failed to start")
        #expect(parser.state.value?.isSpeaking == false)
        #expect(engine.stopCallCount == 1)
    }

    @Test func startListeningHappyPathUpdatesSpeakingTranscriptionAndPowerRatioWithNilError() {
        let micObserver = MockMicrophonePowerObserver()
        let audioSession = MockAudioSession()
        let recognizer = MockSpeechRecognizer()
        let engine = MockAudioEngine()
        let parser = makeParser(
            micObserver: micObserver,
            audioSession: audioSession,
            recognizer: recognizer,
            audioEngine: engine
        )

        parser.startListening(languageCode: "en-US")

        #expect(parser.state.value?.isSpeaking == true)
        #expect(parser.state.value?.error == nil)
        #expect(engine.prepareCallCount == 1)
        #expect(engine.startCallCount == 1)
        #expect(engine.mockInputNode.installTapCallCount == 1)
        #expect(micObserver.startObservingCallCount == 1)
        #expect(audioSession.setCategoryCalls.count == 1)

        recognizer.sendResult("Hello world")
        #expect(parser.state.value?.result == "Hello world")
        #expect(parser.state.value?.error == nil)

        micObserver.subject.send(0.75)
        #expect(parser.state.value?.powerRatio == Float(0.75))
        #expect(parser.state.value?.error == nil)
    }

    @Test func startListeningClearsPreviousErrorToNil() {
        let recognizer = MockSpeechRecognizer(isAvailable: false)
        let parser = makeParser(recognizer: recognizer)

        parser.startListening(languageCode: "en-US")
        #expect(parser.state.value?.error == "Speech recognizer is not available")

        recognizer.isAvailable = true
        parser.startListening(languageCode: "en-US")

        #expect(parser.state.value?.error == nil)
        #expect(parser.state.value?.isSpeaking == true)
    }

    @Test func recognitionErrorUpdatesStateWhileListeningAndIgnoresPostCancelErrors() {
        struct RecognitionFailure: LocalizedError {
            var errorDescription: String? { "Speech network error" }
        }
        let recognizer = MockSpeechRecognizer()
        let parser = makeParser(recognizer: recognizer)

        parser.startListening(languageCode: "en-US")
        recognizer.sendResult(nil, error: RecognitionFailure())
        #expect(parser.state.value?.error == "Speech network error")

        parser.reset()
        #expect(parser.state.value?.error == nil)

        // Simulate late cancellation callback from SFSpeechRecognitionTask after stop/reset
        recognizer.sendResult(nil, error: RecognitionFailure())
        #expect(parser.state.value?.error == nil)
    }

    @Test func stopListeningCancelAndResetCleanUpResources() {
        let micObserver = MockMicrophonePowerObserver()
        let audioSession = MockAudioSession()
        let recognizer = MockSpeechRecognizer()
        let engine = MockAudioEngine()
        let parser = makeParser(
            micObserver: micObserver,
            audioSession: audioSession,
            recognizer: recognizer,
            audioEngine: engine
        )

        parser.startListening(languageCode: "en-US")
        recognizer.sendResult("Testing")
        micObserver.subject.send(0.5)

        parser.cancel()
        #expect(parser.state.value?.isSpeaking == false)
        #expect(parser.state.value?.result == "Testing")
        #expect(micObserver.releaseCallCount == 1)
        #expect(recognizer.lastTask?.cancelCallCount == 1)
        #expect(engine.stopCallCount == 1)
        #expect(engine.mockInputNode.removeTapCallCount == 1)
        #expect(audioSession.setActiveCalls.last?.0 == false)

        parser.reset()
        #expect(parser.state.value?.result == "")
        #expect(parser.state.value?.error == nil)
        #expect(parser.state.value?.powerRatio == 0.0)
        #expect(parser.state.value?.isSpeaking == false)
    }

    @Test func lateRecognitionCallbackFromStoppedSessionDoesNotOverwriteActiveSession() {
        struct StaleFailure: LocalizedError {
            var errorDescription: String? { "Stale session A error" }
        }
        let recognizer = MockSpeechRecognizer()
        let parser = makeParser(recognizer: recognizer)

        parser.startListening(languageCode: "en-US")
        recognizer.sendResult(at: 0, result: "Session A")
        #expect(parser.state.value?.result == "Session A")

        parser.stopListening()
        parser.startListening(languageCode: "en-US")
        recognizer.sendResult(at: 1, result: "Session B")
        #expect(parser.state.value?.result == "Session B")
        #expect(parser.state.value?.error == nil)

        // Deliver late callbacks from session A after session B is active
        recognizer.sendResult(at: 0, result: "Stale session A result")
        recognizer.sendResult(at: 0, result: nil, error: StaleFailure())

        #expect(parser.state.value?.result == "Session B")
        #expect(parser.state.value?.error == nil)
        #expect(parser.state.value?.isSpeaking == true)
    }

    @Test func deferredSpeechAuthorizationAfterStopOrResetDoesNotStartCapture() {
        let micObserver = MockMicrophonePowerObserver()
        let engine = MockAudioEngine()
        let recognizer = MockSpeechRecognizer()
        var pendingAuthCompletion: ((SFSpeechRecognizerAuthorizationStatus) -> Void)?

        let parser = IOSVoiceToTextParser(
            micObserver: micObserver,
            audioSession: MockAudioSession(),
            supportedLocalesProvider: { [Locale(identifier: "en-US")] },
            recognizerFactory: { _ in recognizer },
            speechAuthorizationRequester: { completion in
                pendingAuthCompletion = completion
            },
            audioEngineFactory: { engine }
        )

        parser.startListening(languageCode: "en-US")
        #expect(pendingAuthCompletion != nil)

        parser.stopListening()
        pendingAuthCompletion?(.authorized)

        #expect(micObserver.startObservingCallCount == 0)
        #expect(engine.startCallCount == 0)
        #expect(engine.mockInputNode.installTapCallCount == 0)
        #expect(recognizer.lastTask == nil)
        #expect(parser.state.value?.isSpeaking == false)

        pendingAuthCompletion = nil
        parser.startListening(languageCode: "en-US")
        #expect(pendingAuthCompletion != nil)

        parser.reset()
        pendingAuthCompletion?(.authorized)

        #expect(micObserver.startObservingCallCount == 0)
        #expect(engine.startCallCount == 0)
        #expect(recognizer.lastTask == nil)
        #expect(parser.state.value?.isSpeaking == false)
    }
}
