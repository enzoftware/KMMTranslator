//
//  VoiceRecorderButton.swift
//  iosApp
//
//  Created by Enzo Lizama on 24/02/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct VoiceRecorderButton: View {
    var displayState: DisplayState
    var livePower: Double = 0
    var onClick: () -> Void

    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var isBreathing = false
    @State private var tapCount = 0

    private var buttonColor: Color {
        switch displayState {
        case .speaking:
            return CozyTheme.Colors.terracotta
        case .displayingResults:
            return CozyTheme.Colors.sage
        default:
            return CozyTheme.Colors.indigo
        }
    }

    private var symbolName: String {
        switch displayState {
        case .speaking:
            return "stop.fill"
        case .displayingResults:
            return "checkmark"
        default:
            return "mic.fill"
        }
    }

    private var accessibilityTitle: String {
        switch displayState {
        case .speaking:
            return "Stop recording"
        case .displayingResults:
            return "Use spoken text"
        case .error:
            return "Retry voice recording"
        default:
            return "Start recording"
        }
    }

    var body: some View {
        Button {
            tapCount += 1
            onClick()
        } label: {
            ZStack {
                Color.clear
                    .frame(width: 96, height: 96)
                    .contentShape(Circle())

                if displayState == .speaking && !reduceMotion {
                    Circle()
                        .fill(buttonColor.opacity(0.18))
                        .frame(width: 96, height: 96)
                        .scaleEffect((isBreathing ? 1.18 : 1.0) + min(max(livePower, 0), 1) * 0.22)
                        .animation(CozyTheme.Motion.snappySpring, value: livePower)
                        .transition(.scale.combined(with: .opacity))

                    Circle()
                        .stroke(buttonColor.opacity(0.32), lineWidth: 2)
                        .frame(width: 84, height: 84)
                        .scaleEffect(isBreathing ? 1.12 : 0.98)
                        .transition(.scale.combined(with: .opacity))
                }

                Circle()
                    .fill(buttonColor)
                    .frame(width: 74, height: 74)
                    .shadow(
                        color: buttonColor.opacity(0.35),
                        radius: 14,
                        x: 0,
                        y: 6
                    )

                Image(systemName: symbolName)
                    .font(.system(size: 26, weight: .bold))
                    .foregroundStyle(CozyTheme.Colors.onIndigo)
                    .contentTransition(.symbolEffect(.replace))
            }
            .frame(width: 108, height: 108)
            .contentShape(Circle())
            .animation(CozyTheme.Motion.cozySpring, value: displayState)
        }
        .buttonStyle(CozyScaleButtonStyle(pressedScale: 0.92))
        .sensoryFeedback(.impact(weight: .medium), trigger: tapCount)
        .sensoryFeedback(.impact(weight: .medium), trigger: displayState)
        .accessibilityLabel(accessibilityTitle)
        .onChange(of: displayState) { _, newValue in
            updateBreathing(for: newValue)
        }
        .onAppear {
            updateBreathing(for: displayState)
        }
    }

    private func updateBreathing(for state: DisplayState) {
        guard !reduceMotion else {
            isBreathing = false
            return
        }
        if state == .speaking {
            withAnimation(.easeInOut(duration: 1.1).repeatForever(autoreverses: true)) {
                isBreathing = true
            }
        } else {
            withAnimation(CozyTheme.Motion.gentleEase) {
                isBreathing = false
            }
        }
    }
}

#Preview {
    HStack(spacing: 20) {
        VoiceRecorderButton(displayState: .waitingToTalk, onClick: {})
        VoiceRecorderButton(displayState: .speaking, livePower: 0.6, onClick: {})
        VoiceRecorderButton(displayState: .displayingResults, onClick: {})
    }
    .padding()
    .background(CozyAmbientBackground())
}
