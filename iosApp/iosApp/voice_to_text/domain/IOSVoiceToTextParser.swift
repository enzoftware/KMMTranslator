//
//  IOSVoiceToTextParser.swift
//  iosApp
//
//  Created by Enzo Lizama on 23/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import AVFoundation
import Combine
import Foundation
import Speech
import shared

protocol SpeechRecognitionTaskProtocol: AnyObject {
    func cancel()
}

extension SFSpeechRecognitionTask: SpeechRecognitionTaskProtocol {}

protocol SpeechRecognizerProtocol: AnyObject {
    var isAvailable: Bool { get }
    func recognitionTask(
        with request: SFSpeechRecognitionRequest,
        resultHandler: @escaping (String?, Error?) -> Void
    ) -> any SpeechRecognitionTaskProtocol
}

extension SFSpeechRecognizer: SpeechRecognizerProtocol {
    func recognitionTask(
        with request: SFSpeechRecognitionRequest,
        resultHandler: @escaping (String?, Error?) -> Void
    ) -> any SpeechRecognitionTaskProtocol {
        recognitionTask(with: request) { (result: SFSpeechRecognitionResult?, error: Error?) in
            resultHandler(result?.bestTranscription.formattedString, error)
        } as SFSpeechRecognitionTask
    }
}

protocol AudioSessionProtocol: AnyObject {
    func requestRecordPermission(_ response: @escaping (Bool) -> Void)
    func setCategory(
        _ category: AVAudioSession.Category,
        mode: AVAudioSession.Mode,
        options: AVAudioSession.CategoryOptions
    ) throws
    func setActive(_ active: Bool, options: AVAudioSession.SetActiveOptions) throws
}

extension AVAudioSession: AudioSessionProtocol {}

struct AudioFormatMetrics {
    let sampleRate: Double
    let channelCount: AVAudioChannelCount
    let isOtherFormat: Bool
}

protocol AudioInputNodeProtocol: AnyObject {
    func outputFormat(forBus bus: AVAudioNodeBus) -> AVAudioFormat
    func outputFormatMetrics(forBus bus: AVAudioNodeBus) -> AudioFormatMetrics
    func installTap(
        onBus bus: AVAudioNodeBus,
        bufferSize: AVAudioFrameCount,
        format: AVAudioFormat?,
        block: @escaping AVAudioNodeTapBlock
    )
    func removeTap(onBus bus: AVAudioNodeBus)
}

extension AVAudioInputNode: AudioInputNodeProtocol {
    func outputFormatMetrics(forBus bus: AVAudioNodeBus) -> AudioFormatMetrics {
        let format = outputFormat(forBus: bus)
        return AudioFormatMetrics(
            sampleRate: format.sampleRate,
            channelCount: format.channelCount,
            isOtherFormat: format.commonFormat == .otherFormat
        )
    }
}

protocol AudioEngineProtocol: AnyObject {
    var inputNodeProtocol: any AudioInputNodeProtocol { get }
    func prepare()
    func start() throws
    func stop()
}

extension AVAudioEngine: AudioEngineProtocol {
    var inputNodeProtocol: any AudioInputNodeProtocol {
        inputNode
    }
}

class IOSVoiceToTextParser: VoiceToTextParser, ObservableObject {

    private let _state = IOSMutableStateFlow<VoiceToTextParserState>(
        initialValue: VoiceToTextParserState(
            result: "",
            error: nil,
            powerRatio: 0.0,
            isSpeaking: false
        )
    )

    var state: CommonStateFlow<VoiceToTextParserState> { _state }

    private var micObserver: any MicrophonePowerObserving
    var micPowerRatio: AnyPublisher<Double, Never> {
        micObserver.micPowerRatioPublisher
    }

    private var micPowerCancelable: AnyCancellable?

    private var recognizer: (any SpeechRecognizerProtocol)?
    private var audioEngine: (any AudioEngineProtocol)?
    private var inputNode: (any AudioInputNodeProtocol)?
    private var audioBufferRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: (any SpeechRecognitionTaskProtocol)?
    private var audioSession: any AudioSessionProtocol
    private let supportedLocalesProvider: () -> Set<Locale>
    private let recognizerFactory: (Locale) -> (any SpeechRecognizerProtocol)?
    private let speechAuthorizationRequester:
        (@escaping (SFSpeechRecognizerAuthorizationStatus) -> Void) -> Void
    private let audioEngineFactory: () -> any AudioEngineProtocol

    init(
        micObserver: any MicrophonePowerObserving = MicrophonePowerObserver(),
        audioSession: any AudioSessionProtocol = AVAudioSession.sharedInstance(),
        supportedLocalesProvider: @escaping () -> Set<Locale> = {
            SFSpeechRecognizer.supportedLocales()
        },
        recognizerFactory: @escaping (Locale) -> (any SpeechRecognizerProtocol)? = {
            SFSpeechRecognizer(locale: $0)
        },
        speechAuthorizationRequester: @escaping (
            @escaping (SFSpeechRecognizerAuthorizationStatus) -> Void
        ) -> Void = {
            SFSpeechRecognizer.requestAuthorization($0)
        },
        audioEngineFactory: @escaping () -> any AudioEngineProtocol = {
            AVAudioEngine()
        }
    ) {
        self.micObserver = micObserver
        self.audioSession = audioSession
        self.supportedLocalesProvider = supportedLocalesProvider
        self.recognizerFactory = recognizerFactory
        self.speechAuthorizationRequester = speechAuthorizationRequester
        self.audioEngineFactory = audioEngineFactory
    }

    deinit {
        stopListening()
    }

    func cancel() {
        stopListening()
    }

    func reset() {
        self.stopListening()
        _state.value = VoiceToTextParserState(
            result: "",
            error: nil,
            powerRatio: 0.0,
            isSpeaking: false
        )
    }

    func startListening(languageCode: String) {
        updateState(clearError: true)

        let chosenLocale = Locale(identifier: languageCode)
        let supportedLocale =
            supportedLocalesProvider().contains(chosenLocale)
            ? chosenLocale : Locale(identifier: "en-US")

        self.recognizer = recognizerFactory(supportedLocale)

        guard recognizer?.isAvailable == true else {
            updateState(error: "Speech recognizer is not available")
            return
        }

        self.requestPermissions { [weak self] in
            self?.audioBufferRequest = SFSpeechAudioBufferRecognitionRequest()

            guard let audioBufferRequest = self?.audioBufferRequest else {
                return
            }

            self?.recognitionTask = self?.recognizer?.recognitionTask(
                with: audioBufferRequest
            ) { [weak self] (result, error) in
                self?.performOnMain {
                    guard self?.recognitionTask != nil else {
                        return
                    }
                    guard let result else {
                        if let error {
                            self?.updateState(error: error.localizedDescription)
                        }
                        return
                    }

                    self?.updateState(result: result)
                }
            }

            let engine = self?.audioEngineFactory()
            self?.audioEngine = engine
            self?.inputNode = engine?.inputNodeProtocol

            guard let inputNode = self?.inputNode else {
                self?.stopListening()
                self?.updateState(error: "Failed to get audio input node")
                return
            }

            // Use the input node's native format to avoid format mismatches
            let recordingFormat = inputNode.outputFormat(forBus: 0)
            let formatMetrics = inputNode.outputFormatMetrics(forBus: 0)

            // Validate the format - more detailed validation
            guard
                formatMetrics.sampleRate > 0
                    && formatMetrics.channelCount > 0
            else {
                self?.updateState(
                    error:
                        "Invalid audio format from input node - please test on a physical device"
                )
                self?.stopListening()
                return
            }

            // Additional validation for common audio format issues
            guard !formatMetrics.isOtherFormat else {
                self?.updateState(
                    error:
                        "Unsupported audio format - please test on a physical device"
                )
                self?.stopListening()
                return
            }

            inputNode.installTap(
                onBus: 0,
                bufferSize: 1024,
                format: recordingFormat
            ) { [weak self] buffer, _ in
                self?.audioBufferRequest?.append(buffer)
            }

            do {
                try self?.audioSession.setCategory(
                    .playAndRecord,
                    mode: .spokenAudio,
                    options: .duckOthers
                )
                try self?.audioSession.setActive(
                    true,
                    options: .notifyOthersOnDeactivation
                )
                self?.audioEngine?.prepare()

                self?.micObserver.startObserving()
                try self?.audioEngine?.start()
                self?.updateState(isSpeaking: true)
                self?.micPowerCancelable = self?.micPowerRatio.sink {
                    [weak self] ratio in
                    self?.updateState(powerRatio: ratio)
                }
            } catch {
                self?.stopListening()
                self?.updateState(
                    error: error.localizedDescription,
                    isSpeaking: false
                )
            }
        }
    }

    func stopListening() {
        self.updateState(isSpeaking: false)
        micPowerCancelable?.cancel()
        micPowerCancelable = nil
        micObserver.release()
        let task = recognitionTask
        recognitionTask = nil
        task?.cancel()

        audioBufferRequest?.endAudio()
        audioBufferRequest = nil

        audioEngine?.stop()

        inputNode?.removeTap(onBus: 0)
        audioEngine = nil
        inputNode = nil

        try? audioSession.setActive(false, options: [])
    }

    private func requestPermissions(onGranted: @escaping () -> Void) {
        audioSession.requestRecordPermission { [weak self] wasGranted in
            guard wasGranted else {
                self?.performOnMain {
                    self?.updateState(
                        error: "You need to grant permission to record your voice."
                    )
                    self?.stopListening()
                }
                return
            }

            self?.speechAuthorizationRequester { [weak self] status in
                self?.performOnMain {
                    guard status == .authorized else {
                        self?.updateState(
                            error:
                                "You need to grant permission to transcribe audio."
                        )
                        self?.stopListening()
                        return
                    }
                    onGranted()
                }
            }
        }
    }

    private func performOnMain(_ work: @escaping () -> Void) {
        if Thread.isMainThread {
            work()
        } else {
            DispatchQueue.main.async(execute: work)
        }
    }

    private func updateState(
        result: String? = nil,
        error: String? = nil,
        clearError: Bool = false,
        powerRatio: CGFloat? = nil,
        isSpeaking: Bool? = nil
    ) {
        let currentState = _state.value
        _state.value = VoiceToTextParserState(
            result: result ?? currentState?.result ?? "",
            error: clearError ? nil : (error ?? currentState?.error),
            powerRatio: Float(
                powerRatio ?? CGFloat(currentState?.powerRatio ?? 0.0)
            ),
            isSpeaking: isSpeaking ?? currentState?.isSpeaking ?? false
        )
    }

}
