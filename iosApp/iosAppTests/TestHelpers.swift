import AVFoundation
import Combine
import Foundation
import Speech
import shared
@testable import iosApp

final class FakeTranslateClient: TranslateClient {
    var result: Result<String, Error> = .success("Hallo")
    var onTranslate: ((Language, String, Language) async throws -> String)?
    private(set) var calls: [(fromLanguage: Language, fromText: String, toLanguage: Language)] = []

    func translate(
        fromLanguage: Language,
        fromText: String,
        toLanguage: Language
    ) async throws -> String {
        calls.append((fromLanguage: fromLanguage, fromText: fromText, toLanguage: toLanguage))
        if let onTranslate {
            return try await onTranslate(fromLanguage, fromText, toLanguage)
        }
        return try result.get()
    }
}

final class FakeHistoryDataSource: HistoryDataSource {
    private let historyFlow: IOSMutableStateFlow<NSArray>
    private(set) var insertedItems: [HistoryItem] = []

    init(initialHistory: [HistoryItem] = []) {
        self.historyFlow = IOSMutableStateFlow<NSArray>(
            initialValue: initialHistory as NSArray
        )
    }

    func getHistory(coroutineContext: any KotlinCoroutineContext) -> CommonFlow<NSArray> {
        historyFlow
    }

    func insertHistoryItem(item: HistoryItem) async throws {
        insertedItems.append(item)
        var current = (historyFlow.value as? [HistoryItem]) ?? []
        let nextId = KotlinLong(value: Int64(current.count + 1))
        let storedItem = HistoryItem(
            id: item.id ?? nextId,
            fromLanguageCode: item.fromLanguageCode,
            fromText: item.fromText,
            toLanguageCode: item.toLanguageCode,
            toText: item.toText
        )
        current.insert(storedItem, at: 0)
        historyFlow.value = current as NSArray
    }

    func emitHistory(_ items: [HistoryItem]) {
        historyFlow.value = items as NSArray
    }
}

final class FakeVoiceToTextParser: VoiceToTextParser {
    private let _state: IOSMutableStateFlow<VoiceToTextParserState>

    var state: CommonStateFlow<VoiceToTextParserState> { _state }

    private(set) var startListeningLanguageCodes: [String] = []
    private(set) var stopListeningCallCount = 0
    private(set) var cancelCallCount = 0
    private(set) var resetCallCount = 0

    init(
        initialState: VoiceToTextParserState = VoiceToTextParserState(
            result: "",
            error: nil,
            powerRatio: 0.0,
            isSpeaking: false
        )
    ) {
        self._state = IOSMutableStateFlow<VoiceToTextParserState>(
            initialValue: initialState
        )
    }

    func cancel() {
        cancelCallCount += 1
    }

    func reset() {
        resetCallCount += 1
        _state.value = VoiceToTextParserState(
            result: "",
            error: nil,
            powerRatio: 0.0,
            isSpeaking: false
        )
    }

    func startListening(languageCode: String) {
        startListeningLanguageCodes.append(languageCode)
    }

    func stopListening() {
        stopListeningCallCount += 1
    }

    func emitState(
        result: String = "",
        error: String? = nil,
        powerRatio: Float = 0.0,
        isSpeaking: Bool = false
    ) {
        _state.value = VoiceToTextParserState(
            result: result,
            error: error,
            powerRatio: powerRatio,
            isSpeaking: isSpeaking
        )
    }
}

final class MockMicrophonePowerObserver: MicrophonePowerObserving {
    let subject = PassthroughSubject<Double, Never>()
    private(set) var startObservingCallCount = 0
    private(set) var releaseCallCount = 0

    var micPowerRatioPublisher: AnyPublisher<Double, Never> {
        subject.eraseToAnyPublisher()
    }

    func startObserving() {
        startObservingCallCount += 1
    }

    func release() {
        releaseCallCount += 1
    }
}

final class MockAudioSession: AudioSessionProtocol {
    var recordPermissionGranted = true
    var setCategoryError: Error?
    var setActiveError: Error?
    private(set) var requestRecordPermissionCallCount = 0
    private(set) var setCategoryCalls: [(AVAudioSession.Category, AVAudioSession.Mode, AVAudioSession.CategoryOptions)] = []
    private(set) var setActiveCalls: [(Bool, AVAudioSession.SetActiveOptions)] = []

    func requestRecordPermission(_ response: @escaping (Bool) -> Void) {
        requestRecordPermissionCallCount += 1
        response(recordPermissionGranted)
    }

    func setCategory(
        _ category: AVAudioSession.Category,
        mode: AVAudioSession.Mode,
        options: AVAudioSession.CategoryOptions
    ) throws {
        setCategoryCalls.append((category, mode, options))
        if let setCategoryError {
            throw setCategoryError
        }
    }

    func setActive(_ active: Bool, options: AVAudioSession.SetActiveOptions) throws {
        setActiveCalls.append((active, options))
        if active, let setActiveError {
            throw setActiveError
        }
    }
}

final class MockSpeechRecognitionTask: SpeechRecognitionTaskProtocol {
    private(set) var cancelCallCount = 0

    func cancel() {
        cancelCallCount += 1
    }
}

final class MockSpeechRecognizer: SpeechRecognizerProtocol {
    var isAvailable: Bool
    var lastTask: MockSpeechRecognitionTask?
    private(set) var lastResultHandler: ((String?, Error?) -> Void)?
    private(set) var resultHandlers: [(String?, Error?) -> Void] = []

    init(isAvailable: Bool = true) {
        self.isAvailable = isAvailable
    }

    func recognitionTask(
        with request: SFSpeechRecognitionRequest,
        resultHandler: @escaping (String?, Error?) -> Void
    ) -> any SpeechRecognitionTaskProtocol {
        let task = MockSpeechRecognitionTask()
        lastTask = task
        lastResultHandler = resultHandler
        resultHandlers.append(resultHandler)
        return task
    }

    func sendResult(_ result: String?, error: Error? = nil) {
        lastResultHandler?(result, error)
    }

    func sendResult(at index: Int, result: String?, error: Error? = nil) {
        resultHandlers[index](result, error)
    }
}

final class MockAudioInputNode: AudioInputNodeProtocol {
    var formatMetrics: AudioFormatMetrics
    var format: AVAudioFormat
    private(set) var installTapCallCount = 0
    private(set) var removeTapCallCount = 0

    init(
        formatMetrics: AudioFormatMetrics = AudioFormatMetrics(
            sampleRate: 44100,
            channelCount: 1,
            isOtherFormat: false
        ),
        format: AVAudioFormat = AVAudioFormat(standardFormatWithSampleRate: 44100, channels: 1) ?? AVAudioFormat()
    ) {
        self.formatMetrics = formatMetrics
        self.format = format
    }

    func outputFormat(forBus bus: AVAudioNodeBus) -> AVAudioFormat {
        format
    }

    func outputFormatMetrics(forBus bus: AVAudioNodeBus) -> AudioFormatMetrics {
        formatMetrics
    }

    func installTap(
        onBus bus: AVAudioNodeBus,
        bufferSize: AVAudioFrameCount,
        format: AVAudioFormat?,
        block: @escaping AVAudioNodeTapBlock
    ) {
        installTapCallCount += 1
    }

    func removeTap(onBus bus: AVAudioNodeBus) {
        removeTapCallCount += 1
    }
}

final class MockAudioEngine: AudioEngineProtocol {
    let mockInputNode: MockAudioInputNode
    var startError: Error?
    private(set) var prepareCallCount = 0
    private(set) var startCallCount = 0
    private(set) var stopCallCount = 0

    var inputNodeProtocol: any AudioInputNodeProtocol {
        mockInputNode
    }

    init(inputNode: MockAudioInputNode = MockAudioInputNode()) {
        self.mockInputNode = inputNode
    }

    func prepare() {
        prepareCallCount += 1
    }

    func start() throws {
        startCallCount += 1
        if let startError {
            throw startError
        }
    }

    func stop() {
        stopCallCount += 1
    }
}

@MainActor
func waitUntil(
    timeout: Duration = .seconds(3),
    condition: () -> Bool
) async -> Bool {
    let deadline = ContinuousClock.now + timeout
    while !condition() {
        if ContinuousClock.now >= deadline {
            return false
        }
        try? await Task.sleep(for: .milliseconds(20))
    }
    return true
}

final class FakeSavedWordsDataSource: SavedWordsDataSource {
    private let wordsFlow = IOSMutableStateFlow<NSArray>(initialValue: [] as NSArray)

    var words: [SavedWord] { (wordsFlow.value as? [SavedWord]) ?? [] }

    func observeSavedWords() -> CommonFlow<NSArray> {
        wordsFlow
    }

    func save(word: SavedWord) async throws {
        let others = words.filter { !$0.matches(word.entryKey, word.fromLanguageCode, word.toLanguageCode) }
        wordsFlow.value = ([word] + others) as NSArray
    }

    func remove(entryKey: String, fromLanguageCode: String, toLanguageCode: String) async throws {
        wordsFlow.value = words.filter { !$0.matches(entryKey, fromLanguageCode, toLanguageCode) } as NSArray
    }
}

private extension SavedWord {
    func matches(_ key: String, _ from: String, _ to: String) -> Bool {
        entryKey == key && fromLanguageCode == from && toLanguageCode == to
    }
}
