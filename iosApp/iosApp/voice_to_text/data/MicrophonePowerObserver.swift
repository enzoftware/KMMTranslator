//
//  MicrophonePowerObserver.swift
//  iosApp
//
//  Created by Enzo Lizama on 24/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import Combine
import Foundation
import Speech
import shared

protocol MicrophonePowerObserving: AnyObject {
    var micPowerRatioPublisher: AnyPublisher<Double, Never> { get }
    func startObserving()
    func release()
}

class MicrophonePowerObserver: ObservableObject, MicrophonePowerObserving {

    private var cancellable: AnyCancellable? = nil
    private var audioRecorder: AVAudioRecorder? = nil

    @Published private(set) var micPowerRatio = 0.0

    var micPowerRatioPublisher: AnyPublisher<Double, Never> {
        $micPowerRatio.eraseToAnyPublisher()
    }

    private let powerRatioEmissionPerSecond = 20.0

    let tempFile = FileManager.default.temporaryDirectory
        .appendingPathComponent("recording.m4a")

    deinit {
        release()
    }

    static func normalizedPowerRatio(for powerOffset: Float) -> Double {
        if powerOffset < -50 {
            return 0.0
        }
        let normalizedOffset = Double(50 + powerOffset) / 50.0
        return min(max(0.0, normalizedOffset), 1.0)
    }

    func startObserving() {
        release()
        do {
            let recorderSettings = [
                AVFormatIDKey: NSNumber(value: kAudioFormatAppleLossless),
                AVNumberOfChannelsKey: 1,
            ]

            let recorder = try AVAudioRecorder(
                url: tempFile,
                settings: recorderSettings
            )
            recorder.isMeteringEnabled = true
            guard recorder.record() else {
                print(
                    "Failed to start microphone recording."
                )
                return
            }
            self.audioRecorder = recorder

            self.cancellable = Timer.publish(
                every: 1.0 / powerRatioEmissionPerSecond,
                tolerance: 1.0 / powerRatioEmissionPerSecond,
                on: .main,
                in: .common
            )
            .autoconnect()
            .sink { [weak self] _ in
                recorder.updateMeters()

                let powerOffset = recorder.averagePower(forChannel: 0)
                self?.micPowerRatio = Self.normalizedPowerRatio(for: powerOffset)
            }
        } catch {
            print(
                "An error occurred while MicrophonePowerObserver : \(error.localizedDescription)"
            )
        }
    }

    func release() {
        cancellable?.cancel()
        cancellable = nil
        audioRecorder?.stop()
        audioRecorder = nil
        micPowerRatio = 0.0
        try? FileManager.default.removeItem(at: tempFile)
    }
}
