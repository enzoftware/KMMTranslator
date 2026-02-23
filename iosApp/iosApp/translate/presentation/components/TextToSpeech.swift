//
//  TextToSpeech.swift
//  iosApp
//
//  Created by Enzo Lizama on 23/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import Foundation
import AVFoundation

struct TextToSpeech {
    private let synthesizer = AVSpeechSynthesizer()
    
    func speak(text: String, language: String){
        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = AVSpeechSynthesisVoice(language: language)
        utterance.volume = 1.0
        synthesizer.speak(utterance)
    }
}
