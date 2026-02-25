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

class MicrophonePowerObserver: ObservableObject {

    private var cancellable: AnyCancellable? = nil
    private var audioRecorder: AVAudioRecorder? = nil

    @Published private(set) var micPowerRatio = 0.0

    private let powerRatioEmissionPerSecond = 20.0

    func startObserving() {
        release()
        do {
            let recorderSettings = [
                AVFormatIDKey: NSNumber(value: kAudioFormatAppleLossless),
                AVNumberOfChannelsKey: 1,
            ]
            let recorder = try AVAudioRecorder(
                url: URL(fileURLWithPath: "/dev/null", isDirectory: true),
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
                if powerOffset < -50 {
                    self?.micPowerRatio = 0.0
                } else {
                    let normalizedOffset = CGFloat(50 + powerOffset) / 50.0
                    self?.micPowerRatio = normalizedOffset
                }
            }
        } catch {
            print(
                "An error occurred while MicrophonePowerObserver : \(error.localizedDescription)"
            )
        }
    }
    
    func release() {
        cancellable = nil
        audioRecorder?.stop()
        audioRecorder = nil
        micPowerRatio = 0.0
    }
}
