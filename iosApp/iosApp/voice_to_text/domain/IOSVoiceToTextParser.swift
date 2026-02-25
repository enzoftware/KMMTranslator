//
//  IOSVoiceToTextParser.swift
//  iosApp
//
//  Created by Enzo Lizama on 23/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import Combine
import Foundation
import Speech
import shared

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

    private var micObserver = MicrophonePowerObserver()
    var micPowerRatio: Published<Double>.Publisher {
        micObserver.$micPowerRatio
    }

    private var micPowerCancelable: AnyCancellable?

    private var recognizer: SFSpeechRecognizer?
    private var audioEngine: AVAudioEngine?
    private var inputNode: AVAudioInputNode?
    private var audioBufferRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    private var audioSession: AVAudioSession = AVAudioSession.sharedInstance()
    
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
        updateState(error: nil)

        let choosenLocale = Locale(identifier: languageCode)
        let supportedLocale =
            SFSpeechRecognizer.supportedLocales().contains(choosenLocale)
            ? choosenLocale : Locale(identifier: "en-US")

        self.recognizer = SFSpeechRecognizer(locale: supportedLocale)

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
                guard let result = result else {
                    self?.updateState(error: error?.localizedDescription)
                    return
                }

                if result.isFinal {
                    self?.updateState(
                        result: result.bestTranscription.formattedString
                    )
                } else {
                    // Update with partial results for better UX
                    self?.updateState(
                        result: result.bestTranscription.formattedString
                    )
                }
            }

            self?.audioEngine = AVAudioEngine()
            self?.inputNode = self?.audioEngine?.inputNode

            guard let inputNode = self?.inputNode else {
                self?.stopListening()
                self?.updateState(error: "Failed to get audio input node")
                return
            }

            // Use the input node's native format to avoid format mismatches
            let recordingFormat = inputNode.outputFormat(forBus: 0)

            // Validate the format - more detailed validation
            guard
                recordingFormat.sampleRate > 0
                    && recordingFormat.channelCount > 0
            else {
                self?.updateState(
                    error:
                        "Invalid audio format from input node - please test on a physical device"
                )
                self?.stopListening()
                return
            }

            // Additional validation for common audio format issues
            guard recordingFormat.commonFormat != .otherFormat else {
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
            ) { buffer, _ in
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
        recognitionTask?.cancel()
        recognitionTask = nil

        audioBufferRequest?.endAudio()
        audioBufferRequest = nil

        audioEngine?.stop()

        inputNode?.removeTap(onBus: 0)
        audioEngine = nil
        inputNode = nil

        try? audioSession.setActive(false)
    }

    private func requestPermissions(onGranted: @escaping () -> Void) {
        audioSession.requestRecordPermission { [weak self] wasGranted in
            if !wasGranted {
                self?.updateState(
                    error: "You need to grant permission to record your voice."
                )
                self?.stopListening()
                return
            }

            SFSpeechRecognizer.requestAuthorization { [weak self] status in
                DispatchQueue.main.async {
                    if status != .authorized {
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

    private func updateState(
        result: String? = nil,
        error: String? = nil,
        powerRatio: CGFloat? = nil,
        isSpeaking: Bool? = nil
    ) {
        let currentState = _state.value
        _state.value = VoiceToTextParserState(
            result: result ?? currentState?.result ?? "",
            error: error ?? currentState?.error ?? "",
            powerRatio: Float(
                powerRatio ?? CGFloat(currentState?.powerRatio ?? 0.0)
            ),
            isSpeaking: isSpeaking ?? currentState?.isSpeaking ?? false
        )

    }

}
