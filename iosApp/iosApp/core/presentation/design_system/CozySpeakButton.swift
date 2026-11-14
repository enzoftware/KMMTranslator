//
//  CozySpeakButton.swift
//  iosApp
//
//  Created by Antigravity on 2026-04-06.
//

import SwiftUI

struct CozySpeakButton: View {
    let isSpeaking: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 5) {
                Image(systemName: isSpeaking ? "speaker.wave.3.fill" : "speaker.wave.2.fill")
                    .font(.caption.weight(.semibold))
                    .symbolEffect(.variableColor.iterative, isActive: isSpeaking)
                Text(isSpeaking ? "Speaking" : "Listen")
                    .font(.caption.weight(.semibold))
            }
            .foregroundStyle(isSpeaking ? CozyTheme.Colors.onIndigo : CozyTheme.Colors.indigo)
            .padding(.horizontal, 11)
            .padding(.vertical, 6)
            .background(
                Capsule()
                    .fill(isSpeaking ? CozyTheme.Colors.indigo : CozyTheme.Colors.indigoSoft)
            )
            .animation(CozyTheme.Motion.snappySpring, value: isSpeaking)
        }
        .buttonStyle(CozyScaleButtonStyle(pressedScale: 0.94))
        .sensoryFeedback(.selection, trigger: isSpeaking)
        .accessibilityLabel(isSpeaking ? "Stop speaking" : "Speak translation")
    }
}

#Preview("CozySpeakButton") {
    HStack(spacing: 12) {
        CozySpeakButton(isSpeaking: false, action: {})
        CozySpeakButton(isSpeaking: true, action: {})
    }
    .padding(32)
    .background(CozyAmbientBackground())
}
